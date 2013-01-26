(ns alandipert.storage-atom
  (:refer-clojure :exclude [swap! reset!])
  (:require [cljs.reader :refer [read-string]]
            [cljs.core   :as core]))

(defprotocol IStorageBackend
  "Represents a storage resource."
  (-get [this not-found])
  (-commit! [this value] "Commit value to storage at location."))

(defprotocol IStorageAtom
  "An atom persisted to storage."
  (-reset! [a newval]))

(deftype StorageAtom [meta validator watches storage state]
  core/IEquiv
  (-equiv [o other] (identical? o other))

  core/IDeref
  (-deref [_] @state)

  core/IMeta
  (-meta [_] meta)

  core/IPrintWithWriter
  (-pr-writer [a writer opts]
    (-write writer "#<StorageAtom: ")
    (-pr-writer @state writer opts)
    (-write writer ">"))

  core/IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] @watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (core/swap! watches assoc key f) this)
  (-remove-watch [this key]
    (core/swap! watches dissoc key) this)

  IStorageAtom
  (-reset! [a newval]
    (when validator
      (assert (validator newval) "Validator rejected reference state"))
    (let [oldval @state]
      (core/reset! state newval)
      (-commit! storage newval)
      (-notify-watches a oldval newval))
    newval))

(defn atom*
  [initial-state backend opts]
  (StorageAtom. (:meta opts)
                (:validator opts)
                (core/atom {})
                backend
                (core/atom initial-state)))

(deftype StorageBackend [store key]
  IStorageBackend
  (-get [this not-found]
    (if-let [existing (.getItem store (pr-str key))]
      (read-string existing)
      not-found))
  (-commit! [this value]
    (.setItem store (pr-str key) (pr-str value))))

(defn storage-atom
  "Creates and returns an HTML storage-backed atom.  If key in the
   storage is set, its corresponding value is read and becomes the
   initial value.  Otherwise, the initial value is init.

   Takes zero or more additional options:

   :meta metadata-map.  Metadata is *not* persisted.

   :validator validate-fn.  Validator is *not* persisted.

   If metadata-map is supplied, it will become the metadata on the
   atom. validate-fn must be nil or a side-effect-free fn of one
   argument, which will be passed the intended new state on any state
   change. If the new state is unacceptable, the validate-fn should
   return false or throw an exception."
  [init store key & opts]
  (let [backend (StorageBackend. store key)]
    (atom* (-get backend init) backend (apply hash-map opts))))

(defn reset!
  "Sets the value of storage-atom to newval without regard for the
  current value, and writes the new value to storage. Returns newval."
  [storage-atom newval]
  (-reset! storage-atom newval))

(defn swap!
  "Atomically swaps the value of storage-atom to be:
  (apply f current-value-of-atom args), and writes the new value to
  storage.  Returns the value that was swapped in."
  [storage-atom f & args]
  (-reset! storage-atom (apply f @storage-atom args)))