(ns snake-game.store
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :refer [pprint]]
            [cljs.core.match :refer-macros [match]]
            [re-frame.core :as rf]))

(def app-db  (reagent/atom {}))
(def snake { :position [0,0], :points [[1,0], [2,0]] })

(def left 37)
(def right 39)
(def up 38)
(def down 40)

(defn start-tick-loop
  []
  (println "Starting game loop at 350 ms per tick")
  (js/setInterval #(rf/dispatch [:move-direction]) 350))

(defn random-point
  [game-board-size]
  [(rand-int (- (first game-board-size) 1)) (rand-int (- (last game-board-size) 1))])

(defn handle-keydown
  [e]
  ; Not sure why def are not working on pattern match here.
  (match [(.-keyCode e)]
         [37] (rf/dispatch [:set-direction-left])
         [38] (rf/dispatch [:set-direction-up])
         [39] (rf/dispatch [:set-direction-right])
         [40] (rf/dispatch [:set-direction-down])
         :else (println "no matching key")))

(defn snake-x
  [db]
  (first (:position (:snake db))))

(defn snake-y
  [db]
  (last (:position (:snake db))))

(defn increment-points
  [db]
  (conj (:points (:snake db)) (:position (:snake db))))

(defn prepare
  []
  (rf/reg-event-db              ;; sets up initial application state
    :initialize
    (fn [_ _]
      {:board-size [5,10]
       :score-point-pos (random-point [5,10])
       :snake snake
       :direction right
       :game-running? true}))

  (rf/reg-event-db
    :move-direction
    (fn [db _]
      (match [(:direction db)]
        [37] (rf/dispatch [:move-left])
        [38] (rf/dispatch [:move-up])
        [39] (rf/dispatch [:move-right])
        [40] (rf/dispatch [:move-down])
        :else (println "no matching direction"))
      db))

  (rf/reg-event-db
    :set-direction-down
    (fn [db _]
      (assoc db :direction down)))

  (rf/reg-event-db
      :set-direction-left
      (fn [db _]
        (assoc db :direction left)))

  (rf/reg-event-db
      :set-direction-right
      (fn [db _]
        (assoc db :direction right)))

  (rf/reg-event-db
    :set-direction-up
    (fn [db _]
      (assoc db :direction up)))

; TODO: Define behavior on wall hit
  (rf/reg-event-db
    :move-down
    (fn [db _]
      (if (< (snake-x db) (- (first (:board-size db)) 1))
        (assoc db :snake { :position [(+ 1 (snake-x db)), (snake-y db)] :points db })
        db)))

  (rf/reg-event-db
    :move-up
    (fn [db _]
      (if (> (snake-x db) 0)
        (assoc db :snake { :position [(- (snake-x db) 1), (snake-y db)] :points db })
        db)))

  (rf/reg-event-db
    :move-left
    (fn [db _]
      (if (> (snake-y db) 0)
        (assoc db :snake { :position [(snake-x db), (- (snake-y db) 1)] :points db })
                           
        db)))

  (rf/reg-event-db
    :move-right
    (fn [db _]
      (if (< (snake-y db) (- (last (:board-size db)) 1))
        (assoc db :snake { :position [(snake-x db), (+ 1 (snake-y db))] :points db })
        db)))

  (rf/reg-sub
    :board-size
    (fn [db _]
      (:board-size db)))
  
  (rf/reg-sub
    :snake
    (fn [db _]
      (:snake db)))

  (rf/reg-sub
    :score-point-pos
    (fn [db _]
      (:score-point-pos db)))
  
  (rf/reg-sub
    :snake-pos
    (fn [db _]
      (:position (:snake db))))

  (rf/dispatch-sync [:initialize])
)
