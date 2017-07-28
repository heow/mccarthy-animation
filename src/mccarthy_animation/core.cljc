(ns mccarthy-animation.core
  #?(:clj (:gen-class))
  (:require [original-lisp.core :as lisp]
            [quil.core :as quil :include-macros true]
            [quil.middleware :as m]
            [mccarthy-animation.character :as char]
            [mccarthy-animation.lispm :as lispm] ))

(defonce screen-size {:x 320 :y 320})
(defonce sprite-size {:x 64  :y 64})

(defn say [list-of-symbols]
  (apply str (interpose " " list-of-symbols)))

(defn move-up [distance]
  (str "moving up " distance " ..."))

(defn setup []
  (quil/frame-rate 30)   ; Set frame rate to 30 frames per second.
  (quil/color-mode :hsb) ; Set color mode to HSB (HSV) instead of default RGB.
  
  ;; text
  (quil/text-font (quil/create-font "DejaVu Sans" 10 true))

  ;; setup function returns initial state. It contains
  ;; circle color and position.
  {:color 0
   :angle 0
   :bg (quil/load-image "resources/background.png")
   :hero (char/create "fooman" 120 120)
   :lisp-result ""
   :lisp-time 0 })

(defn now [] (quil/millis))

(defn get-keystroke-or-mouse []
  (cond (quil/key-pressed?) (quil/key-as-keyword)
        (quil/mouse-pressed?) :mouse-click
        :else :none))

;; Hack to determine when we want to evaluate some lisp, checks to see what we are
;; and what we're NOT doing.
(defn eval-lisp? [state now keystroke]
  (let [time-at-last-eval (:lisp-time state)]
    (and (< 1000 (- now time-at-last-eval)) ; wait at least a second
         (contains? {:right 1 :e 1 :d 1 :left 1 :a 1 :up 1 :down 1 :mouse-click 1}  keystroke))))

(defn update-state [state]
  (let [now           (now)
        keystroke     (get-keystroke-or-mouse)
        hero-location (char/move screen-size sprite-size (:position (:hero state)) 
                                 (cond (= :right keystroke) 2 (= :e  keystroke)  2 (= :d keystroke) 2 (= :left keystroke) -2 (= :a keystroke) -2 :else 0)
                                 (cond (= :down  keystroke) 2 (= :up keystroke) -2 :else 0) )
        hero-animation (cond (= :right keystroke) :move (= :left keystroke) :move
                             (= :up keystroke)    :move (= :down keystroke) :move
                             :else :stand)]
    ;; TODO this will go away
    (let [rnd-lisp-op     (if (eval-lisp? state now keystroke) (rand-nth lispm/operations) (:lisp-op state))
          new-lisp-script (if (eval-lisp? state now keystroke) (lispm/eval rnd-lisp-op) nil)
          new-lisp-result (if (eval-lisp? state now keystroke) (if (original-lisp.core/atom? new-lisp-script) new-lisp-script (lispm/eval-clojure new-lisp-script)) (:lisp-result state))
          new-lisp-time   (if (eval-lisp? state now keystroke) now (:lisp-time state))]

      ;; this is the new state
      {:color      (mod (+ (:color state) 0.7) 255)
       :angle      (+ (:angle state) 0.01)
       :bg         (:bg state)
       :hero       (assoc (assoc (:hero state) :position (:position hero-location)) :animation hero-animation)
       :lisp-op     rnd-lisp-op
       :lisp-result new-lisp-result
       :lisp-time   new-lisp-time
       }))
  )

(defn animated-keyword [base-name n speed]
  (let [s (* speed (/ (quil/millis) 1000.0))
        x (+ 1 (mod (int s) n))]
    (keyword (str (name base-name) x))))

(defn draw-state [state]  

  ;; clear the sketch by filling it with light-grey
  (quil/background 240)
  (quil/image (:bg state) 0 0)
  
  ;; bg fill color
  (quil/fill (:color state) 255 255)

  ;;(js/console.log (str "hero: " (:position (:hero state))))

  ;; draw hero
  (quil/image (get-in state [:hero :images (animated-keyword (get-in state [:hero :animation]) ((get-in state [:hero :animation]) char/image-counts) 2.0)])
           (get-in state [:hero :position :x])
           (get-in state [:hero :position :y])
           (:x sprite-size)
           (:y sprite-size))
  
  ;; calculate x and y coordinates of the circle.
  (let [angle (:angle state)
        x (* 150 (quil/cos angle))
        y (* 150 (quil/sin angle))]
    
    ;; Move origin point to the center of the sketch.
    (quil/with-translation [(/ (quil/width) 2)
                         (/ (quil/height) 2)]      
      (quil/ellipse x y 100 100) ) ; draw the circle
    )
  
  ;; draw the text?
  (quil/text (str (:lisp-op state) (if (empty? (:lisp-op state)) nil " => ") (:lisp-result state)) 10 300)
  ;(quil/text (str "anim: " (get-in state [:hero :animation]) " count" ((get-in state [:hero :animation]) char/image-counts)) 10 300)
  )

;; ensure additions are reflected in defsketch call
(defonce sketch-opts
  {:host "mccarthy-animation"
   :size [(:x screen-size) (:y screen-size)]
   :setup setup ; setup function called only once, during sketch initialization.
   :update update-state ; update-state is called on each iteration before draw-state.
   :no-start true ; disable autostart
   :draw draw-state
   :title "McCarthy Animation"
   ;; This sketch uses functional-mode middleware.
   ;; Check quil wiki for more info about middlewares and particularly
   ;; functional mode fun-mode.
   :middleware [m/fun-mode]})

;; cljs entry point 
;; TODO think about how to fix this without macros or a real eval
#?(:cljs
   (quil/defsketch mccarthy-animation
     :host (:host sketch-opts)
     :size (:size sketch-opts)
     :setup (:setup sketch-opts)
     :update (:update sketch-opts)
     :no-start (:no-start sketch-opts)
     :draw (:draw sketch-opts)
     :title (:title sketch-opts)
     :middleware (:middleware sketch-opts)
     ))

;; clj application entry point
#?(:clj
   (defn -main [& args]
     (println "App running, look up and enjoy.")
     (quil/sketch
      :host (:host sketch-opts)
      :size (:size sketch-opts)
      :setup (:setup sketch-opts)
      :update (:update sketch-opts)
      :no-start (:no-start sketch-opts)
      :draw (:draw sketch-opts)
      :title (:title sketch-opts)
      :middleware (:middleware sketch-opts)) )) 
