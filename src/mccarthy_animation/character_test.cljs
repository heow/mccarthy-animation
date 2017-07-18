(ns mccarthy-animation.character-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [mccarthy-animation.character :as char]
            [clojure.spec.alpha :as spec]))

(deftest a-test
  (let [screen-size      {:x 255 :y 255} ; 0-255
        sprite-size      {:x 32  :y 32}]

    ;; kid gloves
    (is (= true  (char/move-to? screen-size sprite-size {::char/x 1    ::char/y 1}) ))
    (is (= true  (char/move-to? screen-size sprite-size {::char/x 64   ::char/y 64}) ))

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

    ;; feed it real crap
    (try (char/move-to? screen-size sprite-size {})                           (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
    (try (char/move-to? screen-size sprite-size {::char/x 64 })               (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
    (try (char/move-to? screen-size sprite-size {::char/y 64 })               (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
    (try (char/move-to? screen-size sprite-size {::char/x "foo" ::char/y 0})  (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
    (try (char/move-to? screen-size sprite-size {::char/x 64 ::char/y nil})   (is (= true false)) (catch js/Object e)) ;; TODO: warning cljs specific
    ))

(deftest create
  (let [hero (char/create "foo" nil 51 52)]
    (is (= "foo" (:name hero)))
    (is (= 51 (:x (:position hero))))
    (is (= 52 (:y (:position hero))))
    ))

(cljs.test/run-tests) ; run this from planck
