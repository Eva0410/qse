import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import net.sourceforge.sizeof.SizeOf;
import org.apache.commons.lang3.time.StopWatch;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;


public class BaselineParser {
    
    public String rdfFile = "";
    
    // Constants
    public final String RDFType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
    public final String OntologyClass = "<http://www.w3.org/2002/07/owl#Ontology>";
    
    // Classes, instances, properties
    HashMap<String, HashSet<String>> classToInstances = new HashMap<>();
    HashMap<String, HashSet<String>> classToProperties = new HashMap<>();
    HashMap<String, String> instanceToClass = new HashMap<>();
    HashSet<String> properties = new HashSet<>();
    HashMap<String, HashSet<String>> propertyToTypes = new HashMap<>();
    
    // Bloom Filters
    BloomFilter<CharSequence> subjObjBloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100_000_000, 0.01);
    
    // Constructor
    BaselineParser(String filePath) {
        this.rdfFile = filePath;
    }
    
    public void firstPass() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFile))                           // - Stream of lines ~ Stream <String>
                    .forEach(line -> {                              // - A terminal operation
                        String[] nodes = line.split(" ");     // - Parse a <subject> <predicate> <object> string
                        
                        if (nodes[1].contains(RDFType)) {
                            //Track instances per class
                            if (classToInstances.containsKey(nodes[2])) {
                                classToInstances.get(nodes[2]).add(nodes[0]);
                            } else {
                                HashSet<String> cti = new HashSet<String>() {{ add(nodes[0]); }};
                                classToInstances.put(nodes[2], cti);
                            }
                            
                            // Track classes per instance
                            instanceToClass.put(nodes[0], nodes[2]);
                        }
                        subjObjBloomFilter.put(nodes[0] + nodes[1]);
                        properties.add(nodes[1]);
                    });
            //properties.remove(RDFType);
            //classToInstances.remove(OntologyClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Time Elapsed firstPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    public void secondPass() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            
            properties.forEach(p -> {
                propertyToTypes.put(p, new HashSet<>());
            });
            
            Files.lines(Path.of(rdfFile))                      // - Stream of lines ~ Stream <String>
                    .filter(line -> !line.contains(RDFType))         // Filter RDF type triples
                    .forEach(line -> {                              // - A terminal operation
                        String[] nodes = line.split(" ");     // parse a <subject> <predicate> <object> string
                        //for this triple, I want to know, given a certain predicate, having its object, what is the type of its object, either it is literal or an IRI to some class
                        propertyToTypes.get(nodes[1]).add(instanceToClass.get(nodes[2]));
                    });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Time Elapsed secondPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    public void prioritizeClasses() {
        StopWatch watch = new StopWatch();
        watch.start();
        this.classToInstances = classToInstances.entrySet()
                .stream()
                .sorted(comparingInt(e -> e.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        watch.stop();
        System.out.println("Time Elapsed prioritizeClasses: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    public void propsExtractor() {
        StopWatch watch = new StopWatch();
        watch.start();
        classToInstances.entrySet().forEach((classInstances -> {
            System.out.print(classInstances.getKey() + "-> ");
            StopWatch innerWatch = new StopWatch();
            innerWatch.start();
            classInstances.getValue().forEach(instance -> {
                //instance is a <subject> string, the key is its type like subj rdf:type key
                properties.forEach(p -> {
                    if (subjObjBloomFilter.mightContain(instance + p)) {
                        if (classToProperties.containsKey(classInstances.getKey())) {
                            classToProperties.get(classInstances.getKey()).add(p);
                            
                        } else {
                            HashSet<String> h = new HashSet<String>() {{ add(p); }};
                            classToProperties.put(classInstances.getKey(), h);
                        }
                    }
                });
            });
            System.out.print(" Time Elapsed:" + TimeUnit.MILLISECONDS.toSeconds(innerWatch.getTime()) + " Sec\n");
        }));
        watch.stop();
        System.out.println("Time Elapsed propsExtractor: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    
    public static void main(String[] args) throws Exception {
        String filePath = args[0];
        BaselineParser parser = new BaselineParser(filePath);
        RDFVault rdfVault = new RDFVault();
        
        parser.firstPass();
        
        //System.out.println(parser.classToInstances.size());
        
        /*   parser.classToInstances.forEach((k, v) -> {
            System.out.println(k + " " + v.size());
        });*/
        
        parser.secondPass();
        
        parser.propertyToTypes.forEach((k, v) -> {
            System.out.println(k + " -> " + v);
        });
        
        //System.out.println(parser.properties.size());
        //System.out.println(parser.properties);
        //parser.propsExtractor();
        //parser.classToProperties.forEach((k, v) -> {System.out.println(k + " -> " + v);});
        
        System.out.println(ClassLayout.parseInstance(parser.classToInstances).toPrintable());
        
    }
}
