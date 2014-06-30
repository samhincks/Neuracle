SensorMining
============
This project is an interface for evaluating labeled trial-based timeseries datasets. For a brief (incomplete) tutorial of how to use the interface, see "Interface Tutorial.pdf" 

 --------------------------
 index.jsp, Enforcing HTML structure
 ----------
The structure of the interface is contained in index.jsp, which is styled be css/style.css. Index.jsp contains three primary components: 
  -"topLeft", a canvas for visual representations of datalayers. This region has two 'beanclass' forms, one connecting     it to 'stripes.Action.DataLayersActionBean' and one to stripes.Action.TechniquesActionBean'. In effect, these         beanclasses are a hack for passing variables to the Stripes ActionBean classes. Each variable listed within these     forms will have a corresponding variable in the java class, stripes.action/DataLayersActionBean. 
  -"topRight", which is empty, since all visual components are governed in javascript
  -"bottomLeft" which contains the forms for passing files to the stripes action bean classes. 
  -"bottomRight" which contains the console with id "consoleForm". It has one variable, consoleInput, which is the key to the entire project; we will see later how changing the value of this can be hacked to deliver real time input to the backen. 
  
  Index.jsp loads all the other relevant javascript and css files. 
  
 --------------------------
  events.js, Initializing javascript logic
 ---------
  events.js first initializes, and provides global access to, the critical javascript classes of the interface. Each such class is defined in the file corresponding to its name.
  
  It has two functions, init, called when a pages is reloaded, and reinit, called when every time we return from java (even if its not refreshed, ie an asynchronous call). 
  
  This file includes all logic for defining key-input, clicks etc. 
  
 --------------------------
   javaInterface.js, Java-javascript message passing classes
 ----------------
   This class contains all the logic for passing data to stripes beans. Apart from loading files, which is accomplished by traditional message Stripes load and reload, all communication with the server is asynchronous, meaning the webpage doesn't refresh every time we call the server. This is a very desirable feature, and in the future as much of the server communication as possible should be accomplished using AJAX calls, matching those in this file.
   
   Message passing works by selecting a form defined in index.jsp, modifying one of its parameters with a particular string, then using jQuery's .post method to the actionbean's address. Execution will return to the function listed in the third parameter. 
   
   The action bean will return a JSONobject, with the following variables
      error //.. there was an error in the back-end, display this message in red
      reload //.. a new datalayer has been added to the context, add it to the view
      reloadT //.. a new technique was added to the context, add it to the view
      content //.. message to be displayed in the console area
      
   What might be pass to the various beans?
      - To ConsoleActionBean, we pass strings from the console, which manipulate and evaluate datasets.
      - To DataLayersActionBean, we pass a request to get all available datasets in the context
      - To TechniquesActionBean, we pass a request to see all available techniques
      - To DataLayerActionBean, we pass a request to see its underlying data (this is streamed back and displayed in the    visualization); we also pass a request to see the average performance of classifiers evaluating that data
      - To TechinqueActionBean, we pass requests to see the average performance using that technique on data
   
 --------------------------
   consoleArea.js, logic for client-side message processing and text output
 --------------
   Using the css styles defined in consoleStyle.css, this class styles messages passed from the back-end, displaying     them in the consle area. 
   
   Also contains the logic for parsing the user's message, detecting any locally detectable flaws, and shipping it off    to the asynchronous message passing protocol in javaInterface.js.
   
   
   
 --------------------------
   ChartArea.js + BarChart.js, D3 code for displaying a zoomable bar chart of technique or datalayer performance.
 --------------------------
   When a technique or datalayer has been evaluated, clicking on it calls the function javaInterface.getTechniqueStats, which calls  TechniqueActionBean.getJSON() asynchronously. This function packages the performances of the selected dataset into a JSONObject with the following variables:
      obj.description
       obj.description.id //.. id of the technique
       obj.description.type //.. the type of the technique, machine learning, attribtue selection, etc.
       obj.description.value //.. the particular value of that technique; for machine learning, what algorithm?
      obj.performance
       obj.performance.value //.. average classification accuracy
       obj.performance.expected //.. whatever the chance classification accuracy would be
       obj.performance.label //.. again the name of collection of techinques
     obj.performance.subValues //.. an array of additional performances, whereas the last averaged, this has each individual average that together constitue the average. 
     
   So the server sends back this object, and we intercept it at javaInterface.returnFromTechniqueStats(), which         calls chartArea.displayPerformance with that object. This clears any existing chart, before instantiating a new       one with the specific parameters set in the JSON object.  
   
   
   
   ---
    
     
     
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
