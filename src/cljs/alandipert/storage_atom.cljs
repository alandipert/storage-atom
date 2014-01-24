(ns alandipert.storage-atom
  (:require [tailrecursion.cljson :refer [clj->cljson cljson->clj]]))

(defprotocol IStorageBackend
  "Represents a storage resource."
  (-get [this not-found])
  (-commit! [this value] "Commit value to storage at location."))

(deftype StorageBackend [store key]
  IStorageBackend
  (-get [this not-found]
    (if-let [existing (.getItem store (clj->cljson key))]
      (cljson->clj existing)
      not-found))
  (-commit! [this value]
    (.setItem store (clj->cljson key) (clj->cljson value))))

(defn store
  [atom backend]
  (let [existing (-get backend ::none)]
    (if (= ::none existing)
      (-commit! backend @atom)
      (reset! atom existing))
    (doto atom
      (add-watch ::storage-watch #(-commit! backend %4)))))

(defn html-storage
  [atom storage k]
  (store atom (StorageBackend. storage k)))

(defn local-storage
  [atom k]
  (html-storage atom js/localStorage k))

(defn session-storage
  [atom k]
  (html-storage atom js/sessionStorage k))
