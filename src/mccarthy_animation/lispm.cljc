(ns mccarthy-animation.lispm
  (:require [clojure.spec.alpha :as spec]
            [cljs.js :as cljs] ; TODO warning cljs
            [original-lisp.core :as lisp])
  #?(:clj (:refer-clojure :exclude [eval])))

(defonce env '((version 1)
               (a 1)
               (b 2)
               (c 3)
               (d 4)
               (f (lambda (x) (cons 'a x)))
               (y ((a b) (c d)))
               (saying1 '(hello there cruel world))
               (saying2 '(oh no not again))
               (get (lambda (x) (car (cons x '()))))
               (speak   (lambda () (cons 'mccarthy-animation.core/say (cons saying2 '()))))
               (move-up (lambda () (cons 'mccarthy-animation.core/move-up '(11))))
               ))

(defonce ops-set #{'speak 'move-up})
(defonce operations (map list (apply list ops-set))) ;; convenient ((speak) (move-up))

(spec/def ::operation ops-set)
;(spec/def ::result    string?)
;(spec/def ::time      int?)
;(spec/def ::state (spec/keys :req [::operation ::result ::time]))

;; call up to a local function  "pappa, why is cljs eval so hard?"
(defn eval-clojure [clojure-program]
  #?(:clj (clojure.core/eval clojure-program)
     :cljs (:value
            (cljs/eval (cljs/empty-state)
                       clojure-program
                       {:eval       cljs/js-eval
                        :source-map true
                        ;; :ns         (find-ns 'mccarthy-animation.core) ; TODO why does this not work?
                        :context    :expr}
                       (fn [result] result))) ))

(defn eval
  ([op env-in]
   {:pre [(spec/valid? ::operation (first op))]} ; throw on bogus input, check function name
   (lisp/l-eval op env-in))
  ([op] (eval op env)))
