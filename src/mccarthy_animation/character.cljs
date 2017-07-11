(ns mccarthy-animation.character
  (:require [original-lisp.core :as lisp]
            [cljs.js :refer [empty-state eval js-eval]]
            [clojure.spec.alpha :as spec]
            ))

(def seed-state
  {
   ::position {::x 1 ::y 1}
   :image nil
   })

(spec/def ::x        (spec/and int? #(>= % 0)))
(spec/def ::y        (spec/and int? #(>= % 0)))
(spec/def ::position (spec/keys :req [::x ::y]))

(defn move-to? [screen-size sprite-size new-position]
  (if (not (spec/valid? ::position new-position)) false ; first check the spec by hand, don't throw an error with a :pre assertion
      (cond ;; we don't know the screen size when we compile
        (> (::x new-position) (- (::x screen-size) (::x sprite-size))) false
        (> (::y new-position) (- (::y screen-size) (::y sprite-size))) false
        :else true)))
