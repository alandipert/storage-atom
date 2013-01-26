(ns alandipert.storage-atom
  (:use [cljs.reader :only [read-string]]))

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

(defn store
  [atom backend]
  (let [existing (-get backend ::none)]
    (if (= ::none existing)
      (-commit! backend @atom)
      (reset! atom existing))
    (add-watch atom ::storage-watch #(-commit! backend %4))
    atom))

(defn html-storage
  [atom storage k]
  (store atom (StorageBackend. storage k)))

(defn local-storage
  [atom k]
  (html-storage atom js/localStorage k))

(comment
  
  (def prefs (local-storage (atom {}) ::foo))

  (swap! prefs assoc :bg-color "red")

  (:bg-color @prefs) ;=> "red"

  )
