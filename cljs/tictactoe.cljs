(ns tictactoe
  "Ported from https://github.com/borkdude/tictactoe-cljs"
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(def empty-board [[\- \- \-]
                  [\- \- \-]
                  [\- \- \-]])

(def state (r/atom {:board empty-board :player \X}))

(defn get-board-cell
  ([board row col]
     (get-in board [row col])))

(defn get-player [app-state]
  (-> app-state :game-state :player))

(defn other-player [player]
  (if (= player \X) \O \X))

(defn winner-in-rows? [board player]
  (boolean (some (fn [row] (every? (fn [c] (= c player)) row)) board)))

(defn transposed-board [board]
  (vec (apply map vector board)))

(defn winner-in-cols? [board player]
  (winner-in-rows? (transposed-board board) player))

(defn winner-in-diagonals? [board player]
  (let [diag-coords [[[0 0] [1 1] [2 2]]
                     [[0 2] [1 1] [2 0]]]]
    (boolean (some (fn [coords]
                     (every? (fn [coord]
                               (= player (apply get-board-cell board coord)))
                             coords))
                   diag-coords))))

(defn winner?
  "checks if there is a winner. when called with no args, checks for player X and player O.
returns the character for the winning player, nil if there is no winner"
  ([board]
     (boolean (or (winner? board \X)
                  (winner? board \O))))
  ([board player]
     (when (or (winner-in-rows? board player)
             (winner-in-cols? board player)
             (winner-in-diagonals? board player))
       player)))

(defn full-board?
  [board]
  (let [all-cells (apply concat board)]
    (not-any? #(= % \-) all-cells)))

(defn new-state [old-state row col]
  (if (and (= (get-board-cell (:board old-state) row col) \-)
           (not (winner? (:board old-state))))
    {:board (assoc-in (:board old-state) [row col] (:player old-state))
     :player (other-player (:player old-state))}
    old-state))

(defn tictactoe []
  [:div
   (if (winner? (:board @state))
     (str "The winner is " (other-player (:player @state)))
     (if (full-board? (:board @state))
       "It's a draw"
       (str "Your turn, player " (:player @state))))
   (let [board (-> @state :board)]
     [:table
      [:tbody
       (map-indexed
        (fn [i row]
          ^{:key i}
          [:tr
           (map-indexed (fn [j elt]
                          ^{:key j}
                          [:td {:on-click (fn []
                                            (swap! state new-state i j))}elt])
                        row)])
        board)]])])

(rdom/render [tictactoe] (.getElementById js/document "app"))
