(ns backend.core-test
  (:require [clojure.test :refer :all]
            [backend.core :refer :all]))

(deftest core-works
  (testing "Core module loads successfully"
    (is (true? true))))
