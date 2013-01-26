(ns alandipert.storage-atom.test
  (:require [alandipert.storage-atom :as sa]))

(def a1 (sa/storage-atom {} js/localStorage "k1"))

(swap! a1 assoc :x 10)
(assert (= (:x @a1) 10))

(reset! a1 123)
(assert (= (.getItem js/localStorage "\"k1\"") "123"))

(def cnt (atom 0))
(add-watch a1 :x (fn [_ _ _ _] (swap! cnt inc)))
(reset! a1 {})
(swap! a1 assoc "computers" "rule")
(assert (= 2 @cnt))

(def a2 (sa/storage-atom 0 js/sessionStorage :foo :validator even?))
(assert (= @a2 0))

(def a3 (sa/storage-atom
         {:x {:y {:z 42}}}
         js/localStorage
         "k3"
         :meta {:some :metadata}))

(assert (= (get-in @a3 [:x :y :z]) 42))
(assert (= (:some (meta a3)) :metadata))

(.log js/console "__exit__")
