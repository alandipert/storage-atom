(ns alandipert.storage-atom.test
  (:require [alandipert.storage-atom :refer [local-storage]]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

;;; localStorage tests

(def a1 (local-storage (atom {}) "k1"))
(deftest test-swap
  (swap! a1 assoc :x 10)
  (is (= (:x @a1) 10)))

(def cnt (atom 0))
(deftest test-watch
  (add-watch a1 :x (fn [_ _ _ _] (swap! cnt inc)))
  (reset! a1 {})
  (swap! a1 assoc "computers" "rule")
  (is (= 2 @cnt)))

(def a2 (local-storage (atom 0 :validator even?) :foo))
(deftest test-validation
  (is (= @a2 0)))

;;; Can't test the 'update' event, because it's only fired
;;; when changes come from another window.

(def a3 (local-storage
         (atom {:x {:y {:z 42}}} :meta {:some :metadata}) "k3"))

(deftest test-update
  (is (= (get-in @a3 [:x :y :z]) 42))
  (is (= (:some (meta a3)) :metadata)))


(def a4 (local-storage
         (atom {:xs [1 2 3]})
         "k4"))

(deftest test-collection-types-preserved
  (swap! a4 update :xs conj 4)
  (is (= (peek (get @a4 :xs)) 4))
  (swap! a4 assoc :ys [#{1 2 3}])
  (is (vector? (get @a4 :ys)))
  (is (set? (first (get @a4 :ys))))
  (is (= (get a4 :ys [#{1 2 3}]))))
