(ns mccarthy-animation.character
  (:require [clojure.spec.alpha :as spec]
            [quil.core :as quil :include-macros true]
            [mccarthy-animation.config :as config]))

(defonce image-keys   [:stand1 :blink1 :tap1 :moveL1 :moveL2 :moveR1 :moveR2])

;; define what it is to be a hero
(spec/def ::x        int?)
(spec/def ::y        int?)
(spec/def ::position (spec/keys :req-un [::x ::y]))
(spec/def ::size     (spec/keys :req-un [::x ::y]))
(spec/def ::id       string?)
(spec/def ::name     string?)
(spec/def ::hero     (spec/keys :req-un [::name ::position ::size])) ;; TODO why does id fail?

;; {:stand 1 :move 4}
(defonce image-counts (frequencies (map #(keyword (apply str (take (- (count (name %)) 1) (name %)))) image-keys)))

(defn- load-one-image [image]
  (let [path (str "resources/" (name image) ".png")]
    (quil/request-image path) ))

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
      (= 1 n) "hello"
      (= 100 n) "Everyone needs computer programming. It will be the way we speak to the servants."
      (= 200 n) "During the first three millenia, the Earthmen complained a lot."
      (= 300 n) "He who refuses to do arithmetic is doomed to talk nonsense."
      (= 400 n) "Mankind will probably survive even if it doesn't take my advice."
      (= 500 n) "Cynicism is a cheap substitute for sophistication. You don't actually have to learn anything."
      (= 600 n) "If you want to do good, work on the technology, not on getting power."
      (= 700 n) "Self-righteousness is more dangerous than smoking."
      :else "")))

(defn get-animation-state [direction]
  (cond (= :right direction) :moveR
        (= :left  direction) :moveL
        (= :up    direction) :moveR
        (= :down  direction) :moveL
        :else                (select-state-randomly)))

(defn- animated-keyword [base-name n speed]
  (let [s (* speed (/ (quil/millis) 1000.0))
        x (+ 1 (mod (int s) n))]
    (keyword (str (name base-name) x))))

(defn get-image [hero]
  (let [animation (get-in hero [:animation])]
    (get-in hero [:images (animated-keyword animation (animation image-counts) 2.0)])))

(defn create
  ([name images initial-x initial-y]
   {:id         (gensym "char-")
    :name       name
    :images     images
    :animation  :stand
    :position   {:x initial-x :y initial-y}
    :size       {:x 38 :y 50} ;; TODO: calculate this
    :speed      2
    :halo-angle (rand-int 360)
    })
  ([name initial-x initial-y]
   (create name (load-images) initial-x initial-y)))

(defn- move-to? [screen-size background sprite-size new-position]
  {:pre [(spec/valid? ::position screen-size)  ; throw on bogus input
         (spec/valid? ::position sprite-size)
         (spec/valid? ::position new-position)]}
  (let [hero-width (:x sprite-size)]
    (cond
      (and (> (:x new-position) (- config/scroll-point-right hero-width)) (> (:x (:position background)) (* -1 (- config/background-max-width config/screen-width)))) false
      (and (< (:x new-position) (+ config/scroll-point-left))             (< (:x (:position background)) 0)) false
      (> (:x new-position) (- (:x screen-size) (:x sprite-size))) false
      (> (:y new-position) (- (:y screen-size) (:y sprite-size))) false
      (< (:x new-position) 0) false
      (< (:y new-position) 0) false
      :else true)))

(defn proposed-position [hero direction]
  {:pre [(spec/valid? ::hero hero)]}
  (let [speed   (:speed hero)
        x-delta (cond (= :right direction)       speed
                      (= :left  direction) (* -1 speed)
                      :else 0)
        y-delta (cond (= :down direction)       speed
                      (= :up   direction) (* -1 speed)
                      :else 0)]
    {:x (+ (:x (:position hero)) x-delta)
     :y (+ (:y (:position hero)) y-delta)} ))

(defn ensure-screen-position [background hero direction]
  {:pre [(spec/valid? ::hero hero)]}
  ;; keep the hero on-screen
  (let [new-pos (proposed-position hero direction)]
    (if (move-to? config/screen-size background (:size hero) new-pos)
      new-pos
      (:position hero))))
