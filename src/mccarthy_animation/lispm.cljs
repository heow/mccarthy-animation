(ns mccarthy-animation.lispm
  (:require [clojure.spec.alpha :as spec]
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

(defn eval [op]
  {:pre [(spec/valid? ::operation op)]} ; throw on bogus input
  (lisp/l-eval op env))
