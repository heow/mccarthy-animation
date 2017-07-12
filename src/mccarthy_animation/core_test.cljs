(ns mccarthy-animation.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]] ))

;; so far we don't test core, this is the top-level, it's run
;; TODO test it in phantom js?
(deftest a-test (is (= 1 1)))

(cljs.test/run-tests) ; run this from planck
