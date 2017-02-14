(ns snake-game.store
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :refer [pprint]]
            [snake-game.utils :as utils]
            [cljs.core.match :refer-macros [match]]
            [re-frame.core :as rf]))

(def app-db  (reagent/atom {}))
(def snake { :position [0,0], :points [] })
(def board-size [10 20])
(def left 37)
(def right 39)
(def up 38)
(def down 40)

(defn start-tick-loop
  []
  (println "Starting game loop at 50 ms per tick")
  (js/setInterval #(rf/dispatch [:move-direction]) 75))

(defn random-point
  [game-board-size points-on-board]
  (let [rng-position [(rand-int (- (first game-board-size) 1)) (rand-int (- (last game-board-size) 1))]]
    (if (utils/in? points-on-board rng-position)
      (random-point game-board-size points-on-board)
      rng-position)))

(defn handle-keydown
  [e]
  ; FIXME: Not sure why def are not working on pattern match here.
  (match [(.-keyCode e)]
         [37] (rf/dispatch [:set-direction-left])
         [38] (rf/dispatch [:set-direction-up])
         [39] (rf/dispatch [:set-direction-right])
         [40] (rf/dispatch [:set-direction-down])))
         ;:else (println "no matching key")))

(defn increment-points
  [db]
  (conj (:points (:snake db)) (:position (:snake db))))

(defn cycle-points
  [db]
  (merge db { :snake (merge (:snake db) { :points (conj (drop-last (:points (:snake db))) (:position (:snake db))) })}))

(defn collision-with-item
  [db]
  (if (utils/in? (:points (:snake db)) (:position (:snake db)))
    (assoc db :game-running? false)
    (if (= (:position (:snake db)) (:score-point-pos db))
      (merge db { :score (+ (:score db) 1)
                  :score-point-pos (random-point (:board-size db) (:points (:snake db)))
                  :snake (merge (:snake db) { :points (increment-points db) }) })
      db)))

(defn snake-x
  [db]
  (first (:position (:snake db))))

(defn snake-y
  [db]
  (last (:position (:snake db))))

(defn move-down
  [db]
  (if (< (snake-x db) (- (first (:board-size db)) 1))
    (assoc db :snake { :position [(+ 1 (snake-x db)), (snake-y db)]
                       :points (:points (:snake db)) })
    (assoc db :game-running? false)))

(defn move-up
  [db]
  (if (> (snake-x db) 0)
    (assoc db :snake { :position [(- (snake-x db) 1), (snake-y db)]
                             :points (:points (:snake db)) })
    (assoc db :game-running? false)))

(defn move-left
  [db]
    (if (> (snake-y db) 0)
      (assoc db :snake { :position [(snake-x db), (- (snake-y db) 1)]
                                           :points (:points (:snake db)) })
      (assoc db :game-running? false)))

(defn move-right
  [db]
  (if (< (snake-y db) (- (last (:board-size db)) 1))
    (assoc db :snake { :position [(snake-x db), (+ 1 (snake-y db))]
                       :points (:points (:snake db))})
    (assoc db :game-running? false)))

(defn prepare
  []
  (rf/reg-event-db              ;; sets up initial application state
    :initialize
    (fn [_ _]
      {:score 0
       :board-size board-size
       :score-point-pos (random-point board-size [])
       :snake snake
       :direction right
       :game-running? true}))

  (rf/reg-event-db
    :move-direction
    (fn [db _]
      (if (:game-running? db)
        (cycle-points 
          (collision-with-item 
            (match [(:direction db)]
              [37] (move-left db)
              [38] (move-up db)
              [39] (move-right db)
              [40] (move-down db)
              :else db)))
        db)))

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
    :snake-parts-pos
    (fn [db _]
      (drop 1 (:points (:snake db)))))
  
  (rf/reg-sub
    :snake-pos
    (fn [db _]
      (:position (:snake db))))

  (rf/dispatch-sync [:initialize])
)
