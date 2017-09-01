(ns mccarthy-animation.core
  #?(:clj (:gen-class))
  #?(:clj (:use [clojure.pprint]))
  (:require [quil.core                          :as quil :include-macros true]
            [quil.middleware                    :as quilm]
            [mccarthy-animation.config          :as config]
            [mccarthy-animation.character       :as char]
            [mccarthy-animation.ball            :as ball]
            [mccarthy-animation.lispm           :as lispm]
            [original-lisp.core                 :as lisp]
            [mccarthy-animation.speech-bubble   :as speech]
            [mccarthy-animation.terminal        :as term])
  #?(:clj (:require [clojure.tools.nrepl.server :as nrepl])))

(defn say [list-of-symbols]
  (apply str (interpose " " list-of-symbols)))

(defn move-up [distance]
  (str "moving up " distance " ..."))

(defn setup []
  (quil/frame-rate config/frame-rate)
  (quil/color-mode :rgb) 
  
  (quil/text-font (quil/create-font config/font config/default-font-size true))

  ;; setup function returns initial state. It contains
  ;; circle color and position.
  (let [hero (char/create "fooman" (:x config/hero-init-position) (:y config/hero-init-position) )
        term (term/create -200 250)]
    {:angle 0
     :background    {:image (quil/load-image "resources/background.png") :position {:x (* -1 config/screen-width) :y 0}}
     :hero          hero
     :magic-lambdas (repeatedly (+ 2 (rand-int 5)) #(ball/create "ball" (:x config/screen-size) (:y config/screen-size)))
     :term          term
     :collideables  (list hero term)
     :lisp-result ""
     :lisp-time 0 }))

(defn- get-keystroke-or-mouse []
  (cond (quil/key-pressed?) (quil/key-as-keyword)
        (quil/mouse-pressed?) :mouse-click
        :else :none))

(defn- get-direction [keystroke]
  ;; convert gaming keys to direction
  (cond (= keystroke :a) :left
        (= keystroke :e) :right
        (= keystroke :d) :right
        (= keystroke :w) :up
        (= keystroke :s) :down
        :else keystroke
        ))

;; Hack to determine when we want to evaluate some lisp, checks to see what we are
;; and what we're NOT doing.
(defn- eval-lisp? [state now keystroke]
  (let [time-at-last-eval (:lisp-time state)]
    (and (< 1000 (- now time-at-last-eval)) ; wait at least a second
         (contains? {:right 1 :e 1 :d 1 :left 1 :a 1 :up 1 :down 1 :mouse-click 1}  keystroke))))

;; Ensures the background stays within the 0 to -640 (3 screens) range
(defn- ensure-background-position [pos]
  (let [min-x (* -1 (- config/background-max-width config/screen-width))
        x (:x pos)]
    {:x (cond (< x min-x) min-x
              (> x 0)    0
              :else x)
     :y (:y pos)}))

(defn scroll [direction background hero thing-pos]
  (let [back-pos        (:position background)
        hero-x          (:x (:position hero))
        hero-width      (:x (:size hero))
        min-x           (* -1 (- config/background-max-width config/screen-width))]
    (cond
      ;; don't scroll if we hit the absolute limits of the bg
      (and (>= (:x back-pos) 0)     (= :left direction))  thing-pos 
      (and (<= (:x back-pos) min-x) (= :right direction)) thing-pos
      ;; do scroll if we're at the scroll point and have background room
      ;; also consider the wiggle-room of the background-scroll-speed
      (and (= :right direction) (>= hero-x (- config/scroll-point-right config/background-scroll-speed hero-width)))  {:x (- (:x thing-pos) config/background-scroll-speed) :y (:y thing-pos)} 
      (and (= :left  direction) (<= hero-x (+ config/scroll-point-left  config/background-scroll-speed)))             {:x (+ (:x thing-pos) config/background-scroll-speed) :y (:y thing-pos)}
      :else thing-pos) ))

;; TODO do this for real ...maybe
(defn collision? [collideables]
  (let [a (first collideables)
        b (second collideables)]
    (and
     (and (<=    (:y (:position a)) (+ (:y (:position b))  (:y (:size     b))))
          (>= (+ (:y (:position a))    (:y (:size     a))) (:y (:position b))))
     (and (<=    (:x (:position a)) (+ (:x (:position b))  (:x (:size     b))))
          (>= (+ (:x (:position a))    (:x (:size     a))) (:x (:position b)))) )))

(defn update-state [state]
  (let [;; TODO these bits will go away
        now       (quil/millis)
        keystroke (get-keystroke-or-mouse)
        rnd-lisp-op     (if (eval-lisp? state now keystroke) (rand-nth lispm/operations) (:lisp-op state))
        new-lisp-script (if (eval-lisp? state now keystroke) (lispm/eval rnd-lisp-op) nil)
        new-lisp-result (if (eval-lisp? state now keystroke) (if (original-lisp.core/atom? new-lisp-script) new-lisp-script (lispm/eval-clojure new-lisp-script)) (:lisp-result state))
        new-lisp-time   (if (eval-lisp? state now keystroke) now (:lisp-time state))]

    (let [direction (get-direction keystroke)
          hero      (-> (:hero state)
                        (assoc ,,, :position   (char/ensure-screen-position (:background state) (:hero state) direction))
                        (assoc ,,, :halo-angle (+ (:halo-angle (:hero state)) ball/angle-speed))
                        (assoc ,,, :animation  (char/get-animation-state direction)) )
          term      (-> (:term state)
                        (assoc ,,, :position (scroll direction (:background state) (:hero state) (:position (:term state)))))]
      { ;; build the new state
       :background (assoc (:background state) :position (ensure-background-position (scroll direction (:background state) (:hero state) (:position (:background state)))))
       :hero       hero
       :magic-lambdas (map #(assoc % :position (scroll direction (:background state) (:hero state) (ball/aim-at (:position %) (ball/calculate-orbit-target (:hero state) %))))
                           (:magic-lambdas state))
       :term        term
       :collideables(list hero term)
       :lisp-op     rnd-lisp-op
       :lisp-result new-lisp-result
       :lisp-time   new-lisp-time
       })) )

(defn draw-state [state]
  ;; background
  (quil/background config/background-color)
  (quil/image (:image (:background state)) (:x (:position (:background state))) 0)

  ;; set default drawing colors
  (quil/stroke config/default-stroke-color) 
  (quil/fill   config/default-fill-color)
  
  ;;(js/console.log (str "hero: " (:position (:hero state))))

  ;; left aligned, hard-code the sizes in pixels, it's the only way to fly in 1983
  ;; make it vaguely utilitarian by drawing the time
  (quil/text-size 48)
  (quil/text-align :left)
  (quil/text (str (quil/hour) ":" (let [m (str (quil/minute))] (if (= 1 (count m)) (str "0" m) m))) 25 60)
  
  (quil/text-size 28)
  (quil/text-align :right)
  (quil/text (str (quil/month) "/" (quil/day)) 300 45)
  (quil/text-align :left)

  ;; terminal
  (term/draw (:term state) (collision? (:collideables state)))
    
  ;; draw hero
  (let [hero (:hero state)]
    (quil/image (char/get-image hero)
                (get-in hero [:position :x])
                (get-in hero [:position :y])
                (get-in hero [:size :x])
                (get-in hero [:size :y]) )

    ;; optional speach bubble
    (speech/draw hero (char/select-speech-randomly))
    )
  
  ;; draw magic lambda balls
  (quil/text-size 12)
  (quil/fill 255 255 0) ; yellow
  (dorun ;; drawing is I/O and is a side-effect, force it to run
   (map #(quil/text "λ" (get-in % [:position :x]) (get-in % [:position :y]))
        (:magic-lambdas state)))
  
  ;; uncomment to debug"
  (comment quil/text (str "\n  hero " (:position (:hero state))
                          "\n bg    " (:position (:background state))
                          ) 10 280)
  )

;; cljs start
;; TODO think about how to fix this without macros or a real eval
#?(:cljs
   (quil/defsketch mccarthy-animation :host (:host config/sketch-opts) :size (:size config/sketch-opts) :setup setup :update update-state :no-start (:no-start config/sketch-opts) :draw draw-state :title (:title config/sketch-opts) :middleware [quilm/fun-mode] ))

;; clj application start
#?(:clj
   (defn -main [& args]
     (println "App running, look up and enjoy.")
     (if (or (= "debug" (first args)) (= "-debug" (first args)) (= "-d" (first args)))
       (do
         (println "starting nrepl on port 4006, kindly jack in")
         (future (nrepl/start-server :port 4006)))) 
     (quil/sketch
      :features [:exit-on-close]
      :host (:host config/sketch-opts) :size (:size config/sketch-opts) :setup setup :update update-state :no-start (:no-start config/sketch-opts) :draw draw-state :title (:title config/sketch-opts) :middleware [quilm/fun-mode] ) ))
