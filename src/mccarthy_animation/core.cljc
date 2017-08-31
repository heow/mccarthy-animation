(ns mccarthy-animation.core
  #?(:clj (:gen-class))
  #?(:clj (:use [clojure.pprint]))
  (:require [quil.core                          :as quil :include-macros true]
            [quil.middleware                    :as quilm]
            [mccarthy-animation.character       :as char]
            [mccarthy-animation.ball            :as ball]
            [mccarthy-animation.lispm           :as lispm]
            [original-lisp.core                 :as lisp]
            [mccarthy-animation.speech-bubble   :as speech]
            [mccarthy-animation.config          :as config])
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
  {:angle 0
   :background {:image (quil/load-image "resources/background.png")
                :position {:x (* -1 config/screen-width) :y 0}}
   :hero (char/create "fooman" (:x config/hero-init-position) (:y config/hero-init-position) )
   :magic-lambdas (repeatedly (+ 2 (rand-int 5)) #(ball/create "ball" (:x config/screen-size) (:y config/screen-size)))
   :term  {:image (quil/load-image "resources/terminal.png") :position {:x -200 :y 250}}
   :lisp-result ""
   :lisp-time 0 })

(defn now [] (quil/millis))

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

(defn scroll [direction background thing-position]
  (let [pos    thing-position
        min-x (* -1 (- config/background-max-width config/screen-width))]
    (cond (and (>= (:x (:position background)) 0)     (= :left direction))  pos ; don't scroll if we hit the limits of the bg
          (and (<= (:x (:position background)) min-x) (= :right direction)) pos 
          (= :right direction) {:x (- (:x pos) config/background-scroll-speed) :y (:y pos)} ; move it
          (= :left  direction) {:x (+ (:x pos) config/background-scroll-speed) :y (:y pos)}
          :else pos)))

(defn update-state [state]
  (let [now       (now)
        keystroke (get-keystroke-or-mouse)
        direction (get-direction keystroke)
        hero-pos  (char/ensure-position (:hero state) direction)]

    ;; aim toward hero orbit (with offset for each one)
    (let [angle (:angle state)]
      
      ;; TODO this will go away
      (let [rnd-lisp-op     (if (eval-lisp? state now keystroke) (rand-nth lispm/operations) (:lisp-op state))
            new-lisp-script (if (eval-lisp? state now keystroke) (lispm/eval rnd-lisp-op) nil)
            new-lisp-result (if (eval-lisp? state now keystroke) (if (original-lisp.core/atom? new-lisp-script) new-lisp-script (lispm/eval-clojure new-lisp-script)) (:lisp-result state))
            new-lisp-time   (if (eval-lisp? state now keystroke) now (:lisp-time state))]
        
        ;; this is the new state
        {:angle      (+ (:angle state) ball/angle-speed)
         :background (assoc (:background state) :position (ensure-background-position (scroll direction (:background state) (:position (:background state)))))
         :hero       (-> (:hero state)
                         (assoc ,,, :position hero-pos)
                         (assoc ,,, :animation (char/get-animation-state direction)) )
         :magic-lambdas (map #(assoc % :position (scroll direction (:background state) (ball/aim-at (:position %) (ball/calculate-orbit-target angle (:hero state) %))))
                             (:magic-lambdas state))
         :term        (-> (:term state)
                          (assoc ,,, :position (scroll direction (:background state) (:position (:term state)))))
         :lisp-op     rnd-lisp-op
         :lisp-result new-lisp-result
         :lisp-time   new-lisp-time
         }) )))

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
  (quil/with-translation [(:x (:position (:term state))) (:y (:position (:term state)))]
    (quil/fill config/black)
    ;; first layer screen background
    (quil/rect 10 5 20 20) 
    (quil/fill config/white)
    ;; second layer text
    (quil/text-size 4) 
    (quil/text (speech/wrap-line 20 (char/select-speech-randomly)) 10 15)
    ;; third layer is image, with "hole" for screen
    (quil/image (:image (:term state)) 0 0 48 48) )
    
  ;; draw hero
  (let [hero (:hero state)]
    (quil/image (char/get-image hero)
                (get-in hero [:position :x])
                (get-in hero [:position :y])
                (get-in hero [:size :x])
                (get-in hero [:size :y]) )

    ;; optional speach bubble
    (speech/draw hero (char/select-speech-randomly)) )
  
  ;; draw magic lambda balls
  (quil/text-size 12)
  (quil/fill 255 255 0) ; yellow
  (dorun ;; drawing is I/O and is a side-effect, force it to run
   (map #(quil/text "λ" (get-in % [:position :x]) (get-in % [:position :y]))
        (:magic-lambdas state)))
  
  ;; uncomment to debug
  (comment quil/text (str "bg     " (:position (:background state))
                          "\nhero " (:position (:hero state))
                          "\nlmb0 " (int (:x (:position (first (:magic-lambdas state)))))) 10 280)
  )

;; ensure additions are reflected in sketch calls
(defonce sketch-opts
  {:host "mccarthy-animation"
   :size [(:x config/screen-size) (:y config/screen-size)]
   :setup setup ; setup function called only once, during sketch initialization.
   :update update-state ; update-state is called on each iteration before draw-state.
   :no-start true ; disable autostart
   :draw draw-state
   :title "McCarthy Animation"
   :middleware [quilm/fun-mode]})

;; cljs start
;; TODO think about how to fix this without macros or a real eval
#?(:cljs
   (quil/defsketch mccarthy-animation :host (:host sketch-opts) :size (:size sketch-opts) :setup (:setup sketch-opts) :update (:update sketch-opts) :no-start (:no-start sketch-opts) :draw (:draw sketch-opts) :title (:title sketch-opts) :middleware (:middleware sketch-opts) ))

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
      :host (:host sketch-opts) :size (:size sketch-opts) :setup (:setup sketch-opts) :update (:update sketch-opts) :no-start (:no-start sketch-opts) :draw (:draw sketch-opts) :title (:title sketch-opts) :middleware (:middleware sketch-opts) ) ))
