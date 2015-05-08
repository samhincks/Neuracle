/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timeseriestufts.evaluatable;
import filereader.Markers;
import java.io.IOException;  
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import timeseriestufts.evaluation.classifiers.weka.WekaData;
import timeseriestufts.evaluatable.ClassificationAlgorithm;
import timeseriestufts.evaluatable.performances.Prediction;
import timeseriestufts.evaluatable.performances.Predictions;
import timeseriestufts.evaluation.experiment.Classification;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.bi.ChannelSet.Tuple;
import timeseriestufts.kth.streams.bi.Instance;
import timeseriestufts.kth.streams.tri.Experiment;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RegSMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.CVParameterSelection;
import weka.classifiers.meta.GridSearch;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import wlsvm.WLSVM;

/* Build Classifiers from machine learning toolkit Weka
 * Performances:
 * jrip: 2824
 * j48: 2031
 * lmt: 1613
 * nb: 1520
 * tnn: 1650
 * smo: 1488
 * simple: 1479
 * logistic: 1588
 * adaboost 1575
 */
public class WekaClassifier  extends ClassificationAlgorithm{

    public static boolean isFavorite(MLType type) {
        return (type == MLType.adaboost||type == MLType.smo || type == MLType.lmt || type == MLType.tnn);
    }
    //.. Used if a classifier is trained, and saved, to be used for later, potentially realtime classification
    public int timesTrained =0;
    public TechniqueSet lastTechniqueTested;
    public Classification lastTrainedClassification;
    public int lastInstanceLength;
    //---------------------------
    public ArrayList<AttributeSelection> lastAsAlgosUsed;


    public static enum MLType {jrip, j48, lmt, nb, tnn, smo, simple, logistic,adaboost, libsvm, multilayer, cvsmo, gridsmo};
    public MLType mlAlgorithm;
    public WekaData wekaData; // we may want to know filters etc
    private J48 j48;
    private IBk tnn;
    private LMT lmt;
    private SMO smo;
    private LibSVM libsvm;
    private MultilayerPerceptron multilayer;
    private Logistic logistic;
    private NaiveBayes nb;
    private SimpleLogistic simple;
    private JRip jrip;
    private AdaBoostM1 adaboost;
    private CVParameterSelection cvsmo; 
    private GridSearch gridsmo;
    private Instances data;

    //.. name can be J48, 3NN, LMT, Naive, SMO, Logistic, Simple, or Jrip
    public WekaClassifier(MLType mlAlgorithm)   {
        this.mlAlgorithm = mlAlgorithm;      
        this.id= mlAlgorithm.toString();
    }
    
    public WekaClassifier(String mlAlgorithm) throws Exception {
        this.id = mlAlgorithm;
        this.mlAlgorithm = MLType.valueOf(mlAlgorithm);
    }
    
    
    /**Test a trained weka classifier on input experiment. Place predictions 
     * in parameter predictions. Apply the attribute selection algorithms append in
     asAlgosApplied**/
    public void test(Experiment testing, TechniqueSet ts, Predictions predictions,
            ArrayList<weka.filters.supervised.attribute.AttributeSelection> asAlgosApplied) throws Exception  {
       
        //.. extract attributes
        testing.extractAttributes(ts.getFeatureSet());
        weka.core.Instances wInstances = testing.getWekaInstances(false); //.. dont apply specified filters , since we're using old ones

        //.. reapply any attribtue selection algorithms applied during training
        if (asAlgosApplied != null) {
            for (weka.filters.supervised.attribute.AttributeSelection as : asAlgosApplied) {
                wInstances = Filter.useFilter(wInstances, as);
            }
        }

        //.. Retrieve enumeration of instances, then iterate through all and classify
        Enumeration wEnumeration = wInstances.enumerateInstances();
        int index = 0; //.. unfortunately we need to know the condition name too from iterating our version
        while (wEnumeration.hasMoreElements()) {
            //.. my version for knowing condition and its percentage; weka's for classification
            weka.core.Instance wInstance = (weka.core.Instance) wEnumeration.nextElement();
            Instance myInstance = testing.matrixes.get(index);
            Classifier classifier = getClassifier();

            //.. if its conditionless, classify regardless 
            if (myInstance.condition == null) {
                int guess = (int) classifier.classifyInstance(wInstance);
                double[] distribution = classifier.distributionForInstance(wInstance);
                
                //.. save the prediction
                predictions.addPrediction(guess, distribution, index);
            }
            //.. only classify if we have this condition
            else if (testing.classification.hasCondition(myInstance.condition)) {
                int guess = (int) classifier.classifyInstance(wInstance);
                double[] distribution = classifier.distributionForInstance(wInstance);
                //.. save the prediction
                predictions.addPrediction(guess, (int) wInstance.classValue(),
                        myInstance.conditionPercentage, distribution, index);
            }
            index++;
        }
       
    }
    
    /**Make predictions on a channelset by building a new instance at specified points
     * Predictions will be stored in TechniqueSet and Dataset
     * the mlAlgorithm specified by mlAlgorithm must be trained
     * instanceLength denotes how many readingsback to look when an instance is created
     * everyK means how many readings to advance before building a new instance
     * asAlgosApplied = an array of attribute selection algorithms applied to the data (can be null) (these are actual applied filters)
     * If an instance's condition is not contained in the classifcation object, it won't be classified
     **/
    public Predictions testRealStream(Classification c, TechniqueSet ts, Dataset ds, ChannelSet stream, 
            int instanceLength, int everyK, 
            ArrayList<weka.filters.supervised.attribute.AttributeSelection> asAlgosApplied) throws Exception{
        if (instanceLength < 4) throw new Exception("Instance length must be longer than 4");
        if(everyK <1) throw new Exception("New-Instance-Sampling-rate cannot be less 1" );
        
        //.. make Predictions object to save stats
        Predictions retPredictions = new Predictions(ds, ts, c, instanceLength,everyK);

        //.. Retrieve our instance-packed version of the stream and get weka version of it
        Experiment streamingExperiment = stream.getMovingExperiment(c, instanceLength, everyK);
        streamingExperiment.setTechniqueSet(ts);
        
        if (ts.getTransformations() != null) {
            for (Transformation t : lastTechniqueTested.getTransformations().transformations) {
                if (t.for3D) streamingExperiment.manipulate(t, false);
            }
        }
        test(streamingExperiment, ts, retPredictions, asAlgosApplied);
        return retPredictions;
    }
   
    /** Classify the last k datapoints of specified channel
     **/
   public Prediction getLastPrediction(ChannelSet cs) throws Exception {  
       int end =  cs.getFirstChannel().numPoints-1;
       int start = cs.getFirstChannel().numPoints - lastInstanceLength-1;
       
       //.. get last instance, and make a new experiment out of it, since this is the sort of object which can be tested
       Instance instance = cs.getInstance(lastTrainedClassification.name,start,end);
       ArrayList<Instance> instances = new ArrayList();
       instances.add(instance);
       Experiment exp = new Experiment(cs.id,lastTrainedClassification, instances, cs.readingsPerSecond);
       
       //.. if the lastTechniqueTested has transformations associated with it, apply those 
       if (lastTechniqueTested.getTransformations() != null) {
           for (Transformation t : lastTechniqueTested.getTransformations().transformations) {
               if(t.for3D) exp.manipulate(t, false);
           }
       }
         
       //.. retrieve the instances and since we know we only have one, simply classify it
       exp.extractAttributes(lastTechniqueTested.getFeatureSet());
       weka.core.Instances wInstances =exp.getWekaInstances(false);
       Classifier classifier = getClassifier();
       
       //.. Get guess and the confidences of all possible classifications
       int guess = (int) classifier.classifyInstance(wInstances.firstInstance());
       double[] confidences = classifier.distributionForInstance(wInstances.firstInstance());
       
       //.. Retrieve relevant components of classification
       String guessS = lastTrainedClassification.values.get(guess);
       Arrays.sort(confidences);
       double secondLargest = confidences[1];
       double confidence = confidences[guess];
       double pctGreater = confidence / secondLargest;
       Prediction p = new Prediction(guessS, "unknown",  confidence, pctGreater, 0);
       return p; 
   }
   
    
    /**Return the Weka Classifier this object refers to*/
    public Classifier getClassifier() {
        //.. return the appropriate Classifier
        switch (mlAlgorithm) {
            case j48:
                return j48;
            case tnn:
                return tnn;
            case lmt:
                return lmt;  
            case nb:
                return nb;
            case smo:
                return smo;
            case logistic:
                return logistic;  
            case jrip:
                return jrip;
            case simple:
                return simple;            
            case adaboost:
                return adaboost; 
            case multilayer :
                return multilayer;
            case libsvm :
                return libsvm;
            case cvsmo : 
                return cvsmo;
            case gridsmo :
                return gridsmo;
            default:
                return j48;
        }   
    }
    
    /**Build a classifier based on WekaData */
    public Classifier buildClassifier(WekaData _wekaData) throws Exception {
        wekaData = _wekaData;
        data = wekaData.data;
        switch (mlAlgorithm) {
            case j48:
                 buildJ48();
                break;
            case tnn:
                buildTNN();
                break;
            case lmt:
                buildLMT();
                break;
            case nb:
                buildNB();
                break;
            case smo:
                buildSMO();
                break;
            case logistic:
                buildLogistic();
                break;
            case jrip:
                buildJRip();
                break;
            case simple:
                buildSimple(); 
                break;
            case adaboost:
                buildAdaBoost();    
                break;
            case libsvm:
                buildLibSVM();
                break;
            case cvsmo: 
                buildCVSMO();
                break;
            case gridsmo : 
                buildGridSMO();
                break;
            case multilayer :
                buildMultilayer();
                break;
            default:
                buildJ48();
        }    
        
        return this.getClassifier();
    }
    
    /**
     * Build a classifier based on WekaData
     */
    public void buildClassifier(Instances instances) throws Exception {
        data = instances;
        switch (mlAlgorithm) {
            case j48:
                buildJ48();
                break;
            case tnn:
                buildTNN();
                break;
            case lmt:
                buildLMT();
                break;
            case nb:
                buildNB();
                break;
            case smo:
                buildSMO();
                break;
            case logistic:
                buildLogistic();
                break;
            case jrip:
                buildJRip();
                break;
            case simple:
                buildSimple();
                break;
            case adaboost:
                buildAdaBoost();
                break;
            case cvsmo:
                buildCVSMO();
                break;
            case multilayer :
                buildMultilayer();
                break;
            case libsvm :
                buildLibSVM();
                break;
            case gridsmo :
                buildGridSMO();
                break;
            default:
                buildJ48();
        }
    }


    private void buildJ48() throws Exception {
        String[] options = new String[1];
        options[0] = "-U";            // unpruned tree
        j48 = new J48();         // new instance of tree
        j48.setOptions(options);     // set the options
       /* 
         j48 = new CVParameterSelection();
        j48.setClassifier(new J48());
        j48.setNumFolds(5);  // using 5-fold CV
        j48.addCVParameter("C 0.1 0.5 5");*/

        // build and output best options
        j48.buildClassifier(data);
       // System.out.println(Utils.joinOptions(j48.getBestClassifierOptions()));
//        j48.buildClassifier(data);   // build classifier
    }

    private void buildTNN() throws Exception {
        tnn = new IBk();
        tnn.setKNN(3);
        tnn.buildClassifier(data);
    }

    private void buildLMT() throws Exception {
         lmt = new LMT();
        lmt.buildClassifier(data);
    }
    
    private void buildMultilayer() throws Exception {
        System.err.println("Super slow model");
        multilayer = new MultilayerPerceptron();
        multilayer.buildClassifier(data);
    }

    private void buildNB() throws Exception {
        nb = new NaiveBayes();
        nb.buildClassifier(data);
    }
    
    /** -K = what type of kernel to use. When linear (other parameters d'nt matter)
     *   0 = Linear, 78%
     *   1  = Polynomial, 78%.
     *   2 = Radial basis = 17%
     *   3 = Sigmoid  = 26%
     * 
     *   -S =  Set type of SVM (default: 0)
        0 = C-SVC 78% -C does not apply
        1 = nu-SVC // 77% Extremely slow. Much more variable performance
        2 = one-class SVM //.. only one class/ -C aplies to these
        3 = epsilon-SVR //.. only one class
        4 = nu-SVR //.. only cone class
        * 
       -D set the degree. //.. dont mess with
       * 
     *DEFAULT: -S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1
     **/
    private void buildLibSVM() throws Exception {
        libsvm = new LibSVM();
        libsvm.setOptions(weka.core.Utils.splitOptions("-K 1 -G 10"));
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {  @Override public void write(int arg0) throws IOException { }}));
        
       // libsvm.setClassifier(new LibSVM());
        //libsvm.addCVParameter("G 0.001 1 100");
        libsvm.buildClassifier(data);
        System.setOut(out);
    }  
    
    /*SMO Parameters: 
     * -C the complexity constant. 
     * -N whether to 0=normalize, 1=standardize, 2=neither
     * -L the tolerance paramter
     * -K  the kernel to use: 
     * Kernel Parameters: 
     *  -C cache, 0 = full cache. Havent noticed difference iwth caches
     * 
     * Having played around, it seems all defaults are optimal.
     */
    private void buildSMO() throws Exception {
        String[] options = new String[1];
        options[0] = "-U";            // unpruned tree
        smo = new SMO();
        String poly = "\"weka.classifiers.functions.supportVector.PolyKernel -C 0 -E 1.0\"";
        String rbf = "\"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.5\"";
        String puk = "\"weka.classifiers.functions.supportVector.Puk\"";
        String kernel = poly;
       // smo.setOptions(weka.core.Utils.splitOptions("-C 1 -L 0.0010 -P 0.0 E-12 -N 0 -V -1 -W 1 -K " +kernel));
        smo.buildClassifier(data);
     }

    private void buildLogistic() throws Exception {
        logistic = new Logistic();
        logistic.buildClassifier(data);
     }

    private void buildSimple() throws Exception {
        simple = new SimpleLogistic();
        simple.buildClassifier(data);
    }

    private void buildJRip() throws Exception {
        jrip = new JRip();
        jrip.buildClassifier(data);
    }
    
    private void buildAdaBoost() throws Exception {
        adaboost = new AdaBoostM1();
        System.out.println(adaboost.toString());
        adaboost.buildClassifier(data);
    }

    private void buildCVSMO() throws Exception {
        SMO subsmo = new SMO();
        cvsmo = new CVParameterSelection();
        cvsmo.setClassifier(subsmo);
        cvsmo.addCVParameter("C 2 10 8");
        cvsmo.setNumFolds(5);
        cvsmo.buildClassifier(data);
        
    }
    
    /** SMO must have built in parameter optimization, since this advanced
     * grid search does not make a lick of difference. 
     **/
    private void buildGridSMO() throws Exception {
        gridsmo = new GridSearch();
        SMO subsmo = new SMO();
        RBFKernel k = new RBFKernel();
        subsmo.setKernel(k);
        gridsmo.setClassifier(new SMO());
        gridsmo.setXProperty("classifier.c");
        gridsmo.setXMin(1);
        gridsmo.setXMax(16);
        gridsmo.setXExpression("I");
        gridsmo.setYProperty("classifier.kernel.gamma");
        gridsmo.setYMin(-5);
        gridsmo.setYMax(2);
        gridsmo.setYStep(1);
        gridsmo.setYBase(10);
        gridsmo.setYExpression("pow(BASE,I)");
        gridsmo.buildClassifier(data);
    }
    
    
    
    public static void main(String [] args) { 
        try {
            ChannelSet cs = ChannelSet.generate(1, 100);
            Markers markers = Markers.generate(10, 10);
            cs.addMarkers(markers);
            
            Experiment e = Experiment.generate(10,10,100);//..cs.splitByLabel(markers.name);
            //e.printInfo();
            TechniqueSet ts = TechniqueSet.generate();
            Dataset ds = Dataset.generate();
            
            int TEST =13;
            
            if (TEST==13) {
                
                e.train(ts);
            }
            
            if (TEST == 12) {
                AdaBoostM1 ab = new AdaBoostM1();
                System.out.println(ab.getTechnicalInformation().getID());
                System.out.println(ab.toString());

            }
            if (TEST ==0 ) {
                 e.evaluate(ts, ds, TEST);
                 ds.printPredictions();
            }
            
            if (TEST == 1) {
                Tuple<Experiment, ChannelSet> pair = cs.getExperimentAndStream(markers.name, 34, 65);
                pair.y.printInfo();
                System.out.println("------");
                pair.x.printInfo();
                
                WekaClassifier wc = pair.x.train(ts);
                Predictions p  = wc.testRealStream(markers.getClassification(), ts, ds, pair.y, 10, 1, null);
                p.printPredictions();
            }
            else if (TEST ==2) {
                Tuple<Experiment, ChannelSet> pair = cs.getExperimentAndStream(markers.name, 34, 65);
                pair.y.printInfo();
                System.out.println("------");
                pair.x.printInfo();

                WekaClassifier wc = pair.x.train(ts);
                Prediction p = wc.getLastPrediction(cs);
                System.out.println(p.toString());
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }
}
