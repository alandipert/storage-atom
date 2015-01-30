(ns alandipert.storage-atom
  (:require [tailrecursion.cljson :refer [clj->cljson cljson->clj]]
            [goog.Timer :as timer]))

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


(defn debounce-factory
  "Return a function that will always store a future call into the
  same atom. If recalled before the time is elapsed, the call is
  replaced without being executed." []
  (let [f (atom nil)]
    (fn [func ttime]
      (when @f
        (timer/clear @f))
      (reset! f (timer/callOnce func ttime)))))

(def storage-delay
  "Delay in ms before a change is committed to the local storage. If a
new change occurs before the time is elapsed, the old change is
discarded an only the new one is committed."
  (atom 10))

(def ^:dynamic *storage-delay* nil)

(def ^:dynamic *watch-active* true)
;; To prevent a save/load loop when changing the values quickly.

(defn store
  [atom backend]
  (let [existing (-get backend ::none)
        debounce (debounce-factory)]
    (if (= ::none existing)
      (-commit! backend @atom)
      (reset! atom existing))
    (doto atom
      (add-watch ::storage-watch
                 #(when (and *watch-active*
                             (not= %3 %4))
                    (debounce (fn [](-commit! backend %4))
                              (or *storage-delay*
                                  @storage-delay)))))))

(defn maybe-update-backend
  [atom storage k default e]
  (when (identical? storage (.-storageArea e))
    (if (empty? (.-key e)) ;; is all storage is being cleared?
      (binding [*watch-active* false]
        (reset! atom default))
      (when-let [sk (cljson->clj (.-key e))]
        (when (= sk k) ;; is the stored key the one we are looking for?
          (binding [*watch-active* false]
            (reset! atom (if-let [value (.-newValue e)] ;; new value, or is key being removed?
                           (cljson->clj value)
                           default))))))))

(defn link-storage
  [atom storage k]
  (let [default @atom]
    (.addEventListener js/window "storage"
                       #(maybe-update-backend atom storage k default %))))

(defn dispatch-remove-event!
  "Create and dispatch a synthetic StorageEvent. Expects key to be a string.
  An empty key indicates that all storage is being cleared."
  [storage key]
  (let [event (js/StorageEvent. "storage")]
    (.initStorageEvent event "storage" false false key nil nil
                       (-> js/window .-location .-href)
                       storage)
    (.dispatchEvent js/window event)
    nil))

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
  (link-storage atom storage k)
  (store atom (StorageBackend. storage k)))

(defn local-storage
  [atom k]
  (html-storage atom js/localStorage k))

(defn session-storage
  [atom k]
  (html-storage atom js/sessionStorage k))

;; Methods to safely remove items from storage or clear storage entirely.

(defn clear-html-storage!
  "Clear storage and also trigger an event on the current window
  so its atoms will be cleared as well."
  [storage]
  (.clear storage)
  (dispatch-remove-event! storage ""))

(defn clear-local-storage! []
  (clear-html-storage! js/localStorage))

(defn clear-session-storage! []
  (clear-html-storage! js/sessionStorage))

(defn remove-html-storage!
  "Remove key from storage and also trigger an event on the current
  window so its atoms will be cleared as well."
  [storage k]
  (let [key (clj->cljson k)]
    (.removeItem storage key)
    (dispatch-remove-event! storage key)))

(defn remove-local-storage! [k]
  (remove-html-storage! js/localStorage k))

(defn remove-session-storage! [k]
  (remove-html-storage! js/sessionStorage k))
