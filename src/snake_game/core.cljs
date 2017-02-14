(ns snake-game.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.match :refer-macros [match]]
            [snake-game.store :as store]
            [snake-game.utils :as utils]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]))

(enable-console-print!)

(println "Reloading the app !")

(def cell-style {:style {:display "table-cell" :position "relative"}})
(def base-tile-style {:display "block" :height 50 :width 50 })
(def background-tile-style (merge base-tile-style {:position "relative" :z-index -1}))
(def worm-tile-style (merge base-tile-style {:position "absolute" :z-index 1 :top 0 :left 0}))

(defn single-row [pos_x size_y]
  [:div {:key pos_x :style {:display "table-row"}}
   (doall (for [pos_y (range size_y)]
     [:div (merge {:key [pos_x, pos_y]} cell-style)
       [:img {:style background-tile-style :src "grass.png" }]
       (if (= @(rf/subscribe [:score-point-pos]) [pos_x, pos_y])
         [:img {:style worm-tile-style :src "apple.png" }])
       (if (= @(rf/subscribe [:snake-pos]) [pos_x, pos_y])
         [:img {:style worm-tile-style :src "snake_start.png" }])
       (if (utils/in? @(rf/subscribe [:snake-parts-pos]) [pos_x, pos_y])
         [:img {:style worm-tile-style :src "snake_middle.png" }])
      ]))])

(defn render-rows [board-size]
  [:div
   (doall (for [pos_x (range (first board-size))]
     (single-row pos_x (last board-size))))])

(defn ui
  []
  [:div
   [:h1 "A ClojureScript snake-game using reframe and reagent"]
   [:h2 "Made by ddrmanxbxfr as a clojurescript learning project"]
   [:h3
     "Score : "
     (with-out-str (pprint  (:score @re-frame.db/app-db)))
     (if (:game-running? @re-frame.db/app-db)
       " -- Running"
       " -- Game over ! Refresh to restart")]
   (render-rows @(rf/subscribe [:board-size]))
   ;[:pre (with-out-str (pprint (:position (:snake @re-frame.db/app-db))))]
   ;[:pre (with-out-str (pprint @(rf/subscribe [:snake-parts-pos])))]
   ])

(defn ^:export run
  []
  (set! (.-onkeydown js/document) store/handle-keydown)
  (store/prepare)
  (store/start-tick-loop)
  (reagent/render
    [ui]
    (js/document.getElementById "app")))

(run)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ; (swap! app-state update-in [:__figwheel_counter] inc)
  )
