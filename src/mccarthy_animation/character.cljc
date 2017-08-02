(ns mccarthy-animation.character
  (:require [clojure.spec.alpha :as spec]
            [quil.core :as quil :include-macros true]))

(spec/def ::x        int?) ; check overflows elsewhere
(spec/def ::y        int?) ; check overflows elsewhere
(spec/def ::position (spec/keys :req [::x ::y]))

(defonce image-keys   [:stand1 :move1 :move2])

;; {:stand 1 :move 4}
(defonce image-counts (frequencies (map #(keyword (apply str (take (- (count (name %)) 1) (name %)))) image-keys)))

(defn- load-one-image [image]
  (let [path (str "resources/" (name image) ".png")]
    (quil/request-image path)))

(defn- load-images []
  (zipmap image-keys (map load-one-image image-keys)))

(defn get-animation-state [keystroke]
  (cond (= :right keystroke) :move
        (= :left  keystroke) :move
        (= :up    keystroke) :move
        (= :down  keystroke) :move
        :else                :stand))

(defn- animated-keyword [base-name n speed]
  (let [s (* speed (/ (quil/millis) 1000.0))
        x (+ 1 (mod (int s) n))]
    (keyword (str (name base-name) x))))

(defn get-image [hero]
  (let [animation (get-in hero [:animation])]
    (get-in hero [:images (animated-keyword animation (animation image-counts) 2.0)])))

(defn create
  ([name images initial-x initial-y]
     {:name name
      :images images
      :animation :stand
      :sprite-size {:x 0 :y 0}
      :position {:x initial-x :y initial-y}
      })
  ([name initial-x initial-y]
   (create name (load-images) initial-x initial-y)))

(defn move-to? [screen-size sprite-size new-position]
  {:pre [(spec/valid? ::position new-position)]} ; throw on bogus input
  (cond
    (> (::x new-position) (- (:x screen-size) (:x sprite-size))) false
    (> (::y new-position) (- (:y screen-size) (:y sprite-size))) false
    (< (::x new-position) 0) false
    (< (::y new-position) 0) false
    :else true))

(defn move [screen-size sprite-size position x-delta y-delta]
  (let [proposed-x (+ (:x position) x-delta)
        proposed-y (+ (:y position) y-delta)]
    ;; keep him on-screen
    (if (move-to? screen-size sprite-size {::x proposed-x ::y proposed-y})
      {:position {:x proposed-x :y proposed-y }}
      {:position position}) ))
