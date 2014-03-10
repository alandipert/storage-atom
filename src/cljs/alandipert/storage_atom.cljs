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

(def ^:dynamic *watch-active* true)
;; To prevent a save/load loop when changing the values quickly.

(defn store
  [atom backend]
  (let [existing (-get backend ::none)]
    (if (= ::none existing)
      (-commit! backend @atom)
      (reset! atom existing))
    (doto atom
      (add-watch ::storage-watch 
                 #(when (and *watch-active*
                             (not= %3 %4))
                    (-commit! backend %4))))))

(defn maybe-load-backend
  [atom k e]
  (when (not-empty (.-key e))
    (when-let [sk (cljson->clj (.-key e))]
      (when (= sk k) ;; is the stored key the one we are looking for?
        (let [value (cljson->clj (.-newValue e))]
          (binding [*watch-active* false]
            (reset! atom value)))))))

(defn link-storage
  [atom k]
  (.addEventListener js/window "storage"
                     #(maybe-load-backend atom k %)))

;;; mostly for tests

(defn load-html-storage
  [storage k]
  (-get (StorageBackend. storage k) nil))

(defn load-local-storage [k]
  (load-html-storage js/localStorage k))

(defn load-session-storage [k]
  (load-html-storage js/sessionStorage k))

;;; main API

(defn html-storage
  [atom storage k]
  (link-storage atom k)
  (store atom (StorageBackend. storage k)))

(defn local-storage
  [atom k]
  (html-storage atom js/localStorage k))

(defn session-storage
  [atom k]
  (html-storage atom js/sessionStorage k))
