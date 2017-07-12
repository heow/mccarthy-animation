(ns mccarthy-animation.lispm-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [clojure.spec.alpha :as spec]
            [mccarthy-animation.lispm :as lispm] ))

(deftest env-test
  (is (not (nil? (some #{'(version 1)} lispm/env)))))

(deftest eval-test
  (is (not (nil? (lispm/eval '(speak)))))
  (try (lispm/eval '(FAIL)) (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
  )

(defn truthy []  true)
(defn say42  []  42)
(defn say    [s] (str "saying " s))
(defn falsey []  false)

(deftest cljeval-test
  (is (= true  (lispm/eval-clojure '(mccarthy-animation.lispm-test.truthy))))
  (is (= false (lispm/eval-clojure '(mccarthy-animation.lispm-test.falsey))))
  )

;; If you think you're good at meta-programming, try it without support for either
;; numbers or strings
(deftest eval-to-cljs-test
  (is (= "saying 42" (lispm/eval-clojure (lispm/eval '(say '42) '((say (lambda (x) (cons 'mccarthy-animation.lispm-test.say (cons x '())))))))))
  )

(cljs.test/run-tests) ; run this from planck
