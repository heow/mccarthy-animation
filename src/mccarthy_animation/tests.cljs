(ns mccarthy-animation.tests
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [mccarthy-animation.character-test]
            ;; tested via planck
            ;; [mccarthy_animation.core-test] 
            ;; [mccarthy_animation.lispm-test]
            [clojure.spec.alpha :as spec]))

(cljs.test/run-tests) ; run this from planck or repl
