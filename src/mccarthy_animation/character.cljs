(ns mccarthy-animation.character
  (:require [clojure.spec.alpha :as spec] ))

(spec/def ::x        int?) ; check overflows elsewhere
(spec/def ::y        int?) ; check overflows elsewhere
(spec/def ::position (spec/keys :req [::x ::y]))

(defn move-to? [screen-size sprite-size new-position]
  {:pre [(spec/valid? ::position new-position)]} ; throw on bogus input
  (cond
    (> (::x new-position) (- (:x screen-size) (:x sprite-size))) false
    (> (::y new-position) (- (:y screen-size) (:y sprite-size))) false
    (< (::x new-position) 0) false
    (< (::y new-position) 0) false
    :else true))

