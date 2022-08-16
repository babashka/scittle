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

(defn pr-code [code-str]
  (pr-str (str "#_CODE_" code-str "#_CODE_")))

(defn read-code [code-str]
  (when-let [raw-code (second (re-find #"#_CODE_(.+)#_CODE_" code-str))]
    ;; Use read-string to undo escaping of characters by pr-str (e.g. newlines)
    (read-string (str "\"" raw-code "\""))))

(defn load-gist [gist callback]
  (let [set-content (fn [progress-event]
                      (callback (.. progress-event -srcElement -responseText)))
        oreq (js/XMLHttpRequest.)]
    (.addEventListener oreq "load" set-content)
    (.open oreq "GET" (str "https://gist.githubusercontent.com/" gist "/raw"))
    (.send oreq)))


(defn bookmarklet-href [code-str]
  (str "javascript:(function(){"
       "var runCode = function() {
          try {
            scittle.core.eval_string(" (pr-code code-str) ")
          } catch (error) {
            console.log('Error in code', error);
            alert('Error running code, see console')
          }
        };"
       "if(typeof scittle === 'undefined'){"
       (append-tag :script {:src "https://babashka.github.io/scittle/js/scittle.js"
                            :onerror "function(){alert('Error loading ' + this.src)}"
                            :onload "runCode"})
       "} else {
         runCode() }"
       "})();"))

(defn query-params []
  (let [query-str (.substring js/window.location.search 1)]
    (into {}
          (map (fn [pair]
                 (let [[k v] (.split pair "=" 2)]
                   [(keyword (js/decodeURIComponent k))
                    (js/decodeURIComponent v)])))
          (.split query-str "&"))))


(def *initial-name (r/atom nil))
(def *initial-code (r/atom nil))

;; Initialize code
(let [{:keys [gist code name]} (query-params)]
  (cond gist
        (do
          (reset! *initial-name "---")
          (reset! *initial-code ";; loading from gist")
          (load-gist gist (fn [content]
                            (let [[code meta-str] (reverse (clojure.string/split content #";;---+\n"))
                                  {bookmark-name :name} (when meta-str
                                                          (read-string meta-str))]
                              (when bookmark-name
                                (reset! *initial-name bookmark-name))
                              (reset! *initial-code code)))))
        code
        (do
          (reset! *initial-name (or name "My first bookmarklet"))
          (reset! *initial-code code))
        :else
        (do
          (reset! *initial-name "My first bookmarklet")
          (reset! *initial-code (str "; This is the code of your bookmarklet\n"
                                     (pr-str '(js/alert "Hello")))))))

(defn bookmark-name-field [initial-name *bookmark-name]
  (let [*name (r/atom initial-name)]
    [(fn []
       [:input {:type "text"
                :placeholder "The name of the Bookmarklet"
                :value @*name
                :on-change (fn [e]
                             (let [v (.. e -target -value)]
                               (reset! *name v)
                               (reset! *bookmark-name
                                       (if (clojure.string/blank? v)
                                         (str "Bookmarklet " (rand-int 1000))
                                         v))))}])]))

(defn editor [*code]
  [:textarea
   {:rows 10 :cols 80
    :value @*code
    :on-drop (fn [e]
               (let [bookmarklet (js/decodeURIComponent (.. e -dataTransfer (getData "text")))
                     cljs-snippet (read-code bookmarklet)
                     new-code (if cljs-snippet
                                (str "; Extracted snippet\n" cljs-snippet)
                                (str "; Failed to extract snippet\n" bookmarklet))]
                 (js/console.log "Dropped" bookmarklet)
                 (set! (.. e -target -value) new-code)
                 (reset! *code new-code)
                 (.preventDefault e)))
    :on-change (fn [e] (reset! *code (.. e -target -value)))}])



(defn workspace []
  (let [value @*initial-code
        *code (r/atom value)
        bookmark-name @*initial-name
        *bookmark-name (r/atom bookmark-name)]
    [:div
     [bookmark-name-field bookmark-name *bookmark-name]
     [:br]
     [editor *code]
     [:br]
     [:br]
     "Click the following link or drag it to the bookmarks bar: "
     [(fn []
        [(fn [] [:a {:href (bookmarklet-href @*code)} @*bookmark-name])])
      *code]
     [:br]
     [(fn []
        [:a {:href (str "?name=" (js/encodeURIComponent @*bookmark-name)
                        "&code=" (js/encodeURIComponent @*code)
                        "%20")} "Copy this link to share ⤴️"])]]))

(rdom/render [workspace] (.getElementById js/document "app"))
