(ns bookmarklet
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn append-tag [tag {:keys [body onload onerror] :as attributes}]
  (str "var s=document.createElement('" (name tag) "');"
       (clojure.string/join ";" (map (fn [[k v]] (str "s.setAttribute('" (name k) "','" (name v) "')")) (dissoc attributes :body :onload :onerror)))
       (when body
         (str ";s.innerText=" body))
       (when onload
         (str ";s.onload=" onload))
       (when onerror
         (str ";s.onerror=" onerror))
       ";document.body.appendChild(s);"))

(defn append-script-tag [url]
  (append-tag :script {:type "application/javascript" :src url}))

(defn bookmarklet-href [code]
  (str "javascript:(function(){"
       "var runCode = function() {
          try {
            scittle.core.eval_string('" code "')
          } catch (error) {
            console.log('Error in code', error);
            alert('Error running code, see console')          
          }
        };"       
       "if(typeof scittle === 'undefined'){"
       (append-tag :script {:src "https://borkdude.github.io/scittle/js/scittle.js"
                            :onerror "function(){alert('Error loading ' + this.src)}"
                            :onload (str "runCode")
                            })
       "} else { 
         runCode() }"
       "})();"))

(defn workspace []
  (let [value (str "; This is the code of your bookmarklet\n"
                   (pr-str '(js/alert "Hello")))
        *code (r/atom value)
        bookmark-name "Bookmark"
        *bookmark-name (r/atom bookmark-name)]
        
   [:div  
     [:input {:type "text" 
              :placeholder bookmark-name 
              :on-change (fn [e] 
                           (let [v (.. e -target -value)]
                             (reset! *bookmark-name 
                                     (if (clojure.string/blank? v)
                                         bookmark-name
                                         v))))}]
     [:br]       
     [:textarea {:rows 10 :cols 80
                 :on-change (fn [e] (reset! *code (.. e -target -value)))}
                value]
     [:br]
     [:br]
     "Click the link below"[:br]
     "or"[:br]
     "Drag the link to the bookmarks bar" [:br]
     [(fn []
        (let [sanitized (->> (clojure.string/split-lines @*code) (remove #(re-find #"\s*;"%)) (clojure.string/join "\n"))]
          (js/console.log "Loaded", sanitized)
          [(fn [] [:a {:href (bookmarklet-href sanitized)} @*bookmark-name])]              
          
          )) *code]]))

(rdom/render [workspace] (.getElementById js/document "app"))