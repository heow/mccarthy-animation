(ns mccarthy-animation.lispm
  (:require [clojure.spec.alpha :as spec]
            [cljs.js :as cljs] ; TODO warning cljs
            [original-lisp.core :as lisp]))

(def env '((version 1)
           (a 1)
           (b 2)
           (c 3)
           (d 4)
           (f (lambda (x) (cons 'a x)))
           (y ((a b) (c d)))
           (saying1 '(hello there cruel world))
           (saying2 '(oh no not again))
           (get (lambda (x) (car (cons x '()))))
           (speak   (lambda () (cons 'mccarthy-animation.core.say (cons saying2 '()))))
           (move-up (lambda () (cons 'mccarthy-animation.core.move-up '(10))))
           ))

(def ops-set #{'(speak) '(move-up)})
(def operations (apply list ops-set)) ;; conveience

(spec/def ::operation ops-set)
(spec/def ::result    string?)
(spec/def ::time      int?)
(spec/def ::state (spec/keys :req [::operation ::result ::time]))

(comment def seed-state
  {:lisp-op nil
   :lisp-result ""
   :lisp-time 0})

;; call up to a local function, why is eval so hard?
;; TODO: very cljs specific
(defn eval-clojure [clojure-program]
  (:value (cljs/eval (cljs/empty-state)
                     clojure-program
                     {:eval       cljs/js-eval
                      :source-map true
                      ;; :ns         (find-ns 'mccarthy-animation.core) ; TODO why does this not work?
                      :context    :expr}
                     (fn [result] result))) )

(defn eval
  ([op env-in]
;   {:pre [(spec/valid? ::operation op)]} ; throw on bogus input
   (lisp/l-eval op env-in) 
   )
  ([op] (eval op env)))
