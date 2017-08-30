(ns mccarthy-animation.character
  (:require [clojure.spec.alpha :as spec]
            [quil.core :as quil :include-macros true]
            [mccarthy-animation.config :as config]))

(spec/def ::x        int?) ; check overflows elsewhere
(spec/def ::y        int?) ; check overflows elsewhere
(spec/def ::position (spec/keys :req [::x ::y]))

(defonce image-keys   [:stand1 :blink1 :tap1 :move1 :move2])

;; {:stand 1 :move 4}
(defonce image-counts (frequencies (map #(keyword (apply str (take (- (count (name %)) 1) (name %)))) image-keys)))

(defn- load-one-image [image]
  (let [path (str "resources/" (name image) ".png")]
    (quil/request-image path)))

(defn- load-images []
  (zipmap image-keys (map load-one-image image-keys)))

(defn select-state-randomly
  "Selects a state to do things at perceived random times.  However, it's being called
100/second and has to be consistent during the animation.   We use the clock on a 21 second 
timer doing things every now and then. Another option would be to use a seeded PSEUDO-random 
number generator, but to do that rquires dropping into Java or JS and not really worth the
effort."
  []
  (let [timer-cycle 21
        n (mod (int (/ (quil/millis) 1000)) timer-cycle)]
    (cond
      (= 01 n) :blink
      (= 10 n) :tap
      :else :stand)))

(defn select-speech-randomly []
  (let [timer-cycle 701
        n (mod (int (/ (quil/millis) 3000)) timer-cycle)]
    (cond ;; TODO move these into the config http://www-formal.stanford.edu/jmc/sayings.html
      (= 10 n) "hello"
      (= 100 n) "Everyone needs computer programming. It will be the way we speak to the servants."
      (= 200 n) "During the first three millenia, the Earthmen complained a lot."
      (= 300 n) "He who refuses to do arithmetic is doomed to talk nonsense."
      (= 400 n) "Mankind will probably survive even if it doesn't take my advice."
      (= 500 n) "Cynicism is a cheap substitute for sophistication. You don't actually have to learn anything."
      (= 600 n) "If you want to do good, work on the technology, not on getting power."
      (= 700 n) "Self-righteousness is more dangerous than smoking."
      :else "")))

(defn get-animation-state [keystroke]
  (cond (= :right keystroke) :move
        (= :left  keystroke) :move
        (= :up    keystroke) :move
        (= :down  keystroke) :move
        :else                (select-state-randomly)))

(defn- animated-keyword [base-name n speed]
  (let [s (* speed (/ (quil/millis) 1000.0))
        x (+ 1 (mod (int s) n))]
    (keyword (str (name base-name) x))))

(defn get-image [hero]
  (let [animation (get-in hero [:animation])]
    (get-in hero [:images (animated-keyword animation (animation image-counts) 2.0)])))

(defn create
  ([name images initial-x initial-y size-x size-y]
     {:name name
      :images images
      :animation :stand
      :position {:x initial-x :y initial-y}
      :size {:x size-x :y size-y}
      })
  ([name initial-x initial-y]
   (create name (load-images) initial-x initial-y config/hero-size-x config/hero-size-y)))

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
