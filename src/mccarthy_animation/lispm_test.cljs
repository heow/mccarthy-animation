(ns mccarthy-animation.lisp-m
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [clojure.spec.alpha :as spec]
            [mccarthy-animation.lispm :as lispm] ))

(deftest env-test
  (is (not (nil? (some #{'(version 1)} lispm/env)))))

(deftest eval-test
  (is (not (nil? (lispm/eval '(speak)))))
  (try (lispm/eval '(FAIL)) (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
)

(cljs.test/run-tests) ; run this from planck
