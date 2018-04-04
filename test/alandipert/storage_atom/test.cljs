(ns alandipert.storage-atom.test
  (:require [alandipert.storage-atom
             :refer [clear-local-storage! clj->json dispatch-synthetic-event!
                     load-local-storage local-storage remove-local-storage!
                     *storage-delay*]]
            [alandipert.storage-atom.test.macros
             :refer-macros [with-local-storage-testing-scope]]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-local-storage-life-cycle
  (with-local-storage-testing-scope
    (let [a1 (local-storage (atom {:x 1}) "foo")]
      (testing "Initialization to given value"
        (is (= @a1 {:x 1}))
        (is (= (load-local-storage "foo") {:x 1})))
      (testing "Ordinary update"
        (swap! a1 assoc :x 10)
        (is (= @a1 {:x 10}))
        (is (= (load-local-storage "foo") {:x 10})))
      (testing "Update with no actual change"
        (reset! a1 {:x 10})
        (is (= @a1 {:x 10}))
        (is (= (load-local-storage "foo") {:x 10})))
      (testing "Collection types preserved"
        (swap! a1 assoc :ys [#{1 2 3}])
        (is (= @a1 {:x 10 :ys [#{1 2 3}]}))
        (is (= (load-local-storage "foo") {:x 10 :ys [#{1 2 3}]})))
      (let [a2 (local-storage (atom nil) "foo")]
        (testing "Initialization to persisted value"
          (is (= @a2 {:x 10 :ys [#{1 2 3}]}))
          (is (= (load-local-storage "foo") {:x 10 :ys [#{1 2 3}]})))
        (testing "Update propagation"
          (swap! a2 dissoc :ys)
          (is (= (load-local-storage "foo") {:x 10}))
          (is (= @a2 {:x 10}))
          ;; Simulating the resulting event synchronously here
          (dispatch-synthetic-event! js/localStorage
                                     (clj->json "foo")
                                     (clj->json {:x 10}))
          (is (= @a1 {:x 10})))
        (testing "Reset to initial values upon clearing storage"
          (clear-local-storage!)
          (is (= @a1 {:x 1}))
          ;; This is exactly correct but not sure if intended
          (is (= @a2 nil))
          (is (= (load-local-storage "foo") nil)))))))

(deftest test-local-storage-isolation
  (with-local-storage-testing-scope
    (let [foo (local-storage (atom {:x 1}) :foo)
          bar (local-storage (atom {:y 2}) :bar)]
      (testing "Initial state is good"
        (is (= (load-local-storage :foo) {:x 1}))
        (is (= (load-local-storage :bar) {:y 2})))
      (testing "Updates don't interfere"
        (swap! foo assoc :y 3)
        (swap! bar assoc :x 4)
        (is (= @foo {:x 1 :y 3}))
        (is (= @bar {:x 4 :y 2}))
        (is (= (load-local-storage :foo) {:x 1 :y 3}))
        (is (= (load-local-storage :bar) {:x 4 :y 2})))
      (testing "Removing single key doesn't interfere"
        (remove-local-storage! :foo)
        (is (= @foo {:x 1}))
        (is (= @bar {:x 4 :y 2}))
        (is (= (load-local-storage :foo) nil))
        (is (= (load-local-storage :bar) {:x 4 :y 2}))
        (swap! foo assoc :y 5)
        (remove-local-storage! :bar)
        (is (= @foo {:x 1 :y 5}))
        (is (= @bar {:y 2}))
        (is (= (load-local-storage :foo) {:x 1 :y 5}))
        (is (= (load-local-storage :bar) nil))
        (swap! bar assoc :x 6)
        (is (= @foo {:x 1 :y 5}))
        (is (= @bar {:x 6 :y 2}))
        (is (= (load-local-storage :foo) {:x 1 :y 5}))
        (is (= (load-local-storage :bar) {:x 6 :y 2})))
      (testing "Clearing local storage removes everything"
        (clear-local-storage!)
        (is (= @foo {:x 1}))
        (is (= @bar {:y 2}))
        (is (= (load-local-storage :foo) nil))
        (is (= (load-local-storage :bar) nil))))))
