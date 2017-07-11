(ns mccarthy-animation.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]] ))

(deftest a-test (is (= 0 1)))

(cljs.test/run-tests) ; run this from planck
