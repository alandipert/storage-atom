(ns alandipert.storage-atom
  (:require [cljs.reader :refer [read-string]]
            [cljs.core   :as core]))

(defprotocol IStorageBackend
  "Represents a storage resource."
  (-get [this not-found])
  (-commit! [this value] "Commit value to storage at location."))

(deftype StorageBackend [store key]
  IStorageBackend
  (-get [this not-found]
    (if-let [existing (.getItem store (pr-str key))]
      (read-string existing)
      not-found))
  (-commit! [this value]
    (.setItem store (pr-str key) (pr-str value))))

(defn atom* [init backend opts]
  (let [startval (let [existing (-get backend ::none)]
                   (if (= existing ::none) init existing))]
    (doto (apply atom startval opts)
      (add-watch ::storage-watch #(-commit! backend %4))
      (swap! identity))))

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
  (atom* init (StorageBackend. store key) opts))