(ns mccarthy-animation.character-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [mccarthy-animation.character :as char]))

(deftest a-test
  (let [screen-size      {::char/x 255 ::char/y 255} ; 0-255
        sprite-size      {::char/x 32  ::char/y 32}]

    ;; kid gloves
    (is (= true  (char/move-to? screen-size sprite-size {::char/x 1    ::char/y 1}) ))
    (is (= true  (char/move-to? screen-size sprite-size {::char/x 64    ::char/y 64}) ))

    ;; edges, min and max
    (is (= true  (char/move-to? screen-size sprite-size {::char/x 0    ::char/y 0}) ))
    (is (= true  (char/move-to? screen-size sprite-size {::char/x 222  ::char/y 222}) )) ; 256 - 32
    (is (= false (char/move-to? screen-size sprite-size {::char/x 64   ::char/y 999}) ))
    (is (= false (char/move-to? screen-size sprite-size {::char/x 999  ::char/y 64}) ))
    (is (= false (char/move-to? screen-size sprite-size {::char/x 64   ::char/y -999}) ))
    (is (= false (char/move-to? screen-size sprite-size {::char/x -999 ::char/y 64}) ))
    (is (= false (char/move-to? screen-size sprite-size {::char/x 64   ::char/y 224}) )) ; 256 - 32
    (is (= false (char/move-to? screen-size sprite-size {::char/x 224  ::char/y 64}) ))  ; 256 - 32
    (is (= false (char/move-to? screen-size sprite-size {::char/x 256  ::char/y 256}) ))

    ;; feed it crap
    (is (= false  (char/move-to? screen-size sprite-size {}) ))
    (is (= false  (char/move-to? screen-size sprite-size {::char/x 64 }) ))
    (is (= false  (char/move-to? screen-size sprite-size {::char/y 64 }) ))
    (is (= false  (char/move-to? screen-size sprite-size {::char/x "foo" ::char/y 0}) ))
    (is (= false  (char/move-to? screen-size sprite-size {::char/x 64 ::char/y nil}) ))
    ))

(cljs.test/run-tests) ; run this from planck
