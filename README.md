# nm
Normalized Maps + Convention + Multi-methods = Simple Access
## The Point
Discipline yourself with how you describe the data you care about as outlined in JES. After that you hide implementation details behind multi-methods with keyword first argument dispatch. This allows you to...
```clojure
(def add (partial nm/add :my/variant))
```
The situational dopeness and ability to hide this crucial implementation detail of all 4 common access operations. `add` `rm` `change` and `employ`. These names are dumb and legacy and dumb, but legacy for me. It's crud, but I hate calling my functions clojure.core functions when I want to use them like this, so alas `employ` in place of read or view.   
## My Default


What I really want to give to people who love Clojure is a pattern and a boldness in the concerns they employ. It goes like this. These multi-methods are a perfect match for normalized map based crud which is the fixed point fulcrum for state in clojure runtime. The normalized map allows for very fine-grained automatic default behavior by making a first arg in the path vector dispatch. i.e. :person/id. My dude, from there you use lenses to copy everything in the fashion that you want. 

This is something I call datastructure oriented programming. It's built around clear communication long-lived, soap-bubble systems. The point is to have a codebase that at 10K lines rivals in functionality and robustness 1 million lines of Java and is a joy to extend. It's ball of mud programming and the ball of mud is the AAEAVT transaction into normalized maps just enough structure. 
