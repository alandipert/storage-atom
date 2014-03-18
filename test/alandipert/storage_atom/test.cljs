(ns alandipert.storage-atom.test
  (:require [alandipert.storage-atom :refer [local-storage]]
            [tailrecursion.cljson :refer [clj->cljson cljson->clj]]))

;;; localStorage tests

(def a1 (local-storage (atom {}) "k1"))

(swap! a1 assoc :x 10)
(assert (= (:x @a1) 10))

(def cnt (atom 0))
(add-watch a1 :x (fn [_ _ _ _] (swap! cnt inc)))
(reset! a1 {})
(swap! a1 assoc "computers" "rule")
(assert (= 2 @cnt))

(def a2 (local-storage (atom 0 :validator even?) :foo))
(assert (= @a2 0))


;;; Can't test the 'update' event, because it's only fired
;;; when changes come from another window.

(def a3 (local-storage
         (atom {:x {:y {:z 42}}} :meta {:some :metadata}) "k3"))

(assert (= (get-in @a3 [:x :y :z]) 42))
(assert (= (:some (meta a3)) :metadata))


(.log js/console "__exit__")
