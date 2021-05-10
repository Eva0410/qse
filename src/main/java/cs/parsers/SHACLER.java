package cs.parsers;
import org.apache.commons.io.FilenameUtils;
import cs.Main;
import cs.utils.ConfigManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.semanticweb.yars.nx.Node;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class SHACLER {
    String classIRI;
    public final String SHAPES_NAMESPACE = "http://shaclshapes.org/";
    HashMap<Node, HashSet<String>> propToType = null;
    ValueFactory factory = SimpleValueFactory.getInstance();
    Model model = null;
    ModelBuilder builder = null;
    
    
    public SHACLER() {
        this.builder = new ModelBuilder();
        builder.setNamespace("shape", SHAPES_NAMESPACE);
    }
    
    public void setParams(Node classNode, HashMap<Node, HashSet<String>> propToType) {
        this.classIRI = classNode.getLabel();
        this.propToType = propToType;
    }
    
    public void setParams(String classLabel, HashMap<Node, HashSet<String>> propToType) {
        this.classIRI = classLabel;
        this.propToType = propToType;
    }
    
    public void constructShape() {
        Model m = null;
        ModelBuilder b = new ModelBuilder();
        IRI subj = factory.createIRI(this.classIRI);
        
        String nodeShape = "shape:" + subj.getLocalName() + "Shape";
        b.subject(nodeShape)
                .add(RDF.TYPE, SHACL.NODE_SHAPE)
                .add(SHACL.TARGET_CLASS, subj)
                .add(SHACL.IGNORED_PROPERTIES, RDF.TYPE)
                .add(SHACL.CLOSED, false);
        
        if (propToType != null) {
            propToType.forEach((prop, propTypes) -> {
                IRI property = factory.createIRI(prop.getLabel());
                IRI propShape = factory.createIRI("sh:" + property.getLocalName() + subj.getLocalName() + "ShapeProperty");
                b.subject(nodeShape)
                        .add(SHACL.PROPERTY, propShape);
                b.subject(propShape)
                        .add(SHACL.PATH, property)
                        .add(SHACL.MIN_COUNT, 1);
                //.add(SHACL.MAX_COUNT, 1);
                
                propTypes.forEach(type -> {
                    if (type != null) {
                        if (type.contains(XSD.NAMESPACE) || type.contains(RDF.LANGSTRING.toString())) {
                            b.subject(propShape)
                                    .add(SHACL.DATATYPE, type);
                        } else {
                            type = type.replace("<", "").replace(">", "");
                            b.subject(propShape)
                                    .add(SHACL.CLASS, type)
                                    .add(SHACL.NODE_KIND, SHACL.IRI);
                        }
                    } else {
                        // in case the type is null, we set it default as string
                        b.subject(propShape)
                                .add(SHACL.DATATYPE, XSD.STRING);
                    }
                });
            });
        }
        
        m = b.build();
        //printModel(m);
        model = builder.build();
        model.addAll(m);
    }
    
    public Statement createStatement(IRI s, IRI p, IRI o) {
        return factory.createStatement(s, p, o);
    }
    
    public void printModel() {
        Rio.write(model, System.out, RDFFormat.TURTLE);
    }
    
    public void writeModelToFile() {
        Path path = Paths.get(Main.datasetPath);
        String fileName = FilenameUtils.removeExtension(path.getFileName().toString()) + "-SHACL.ttl";
        System.out.println("::: SHACLER ~ WRITING MODEL TO FILE: " + fileName);
        try {
            FileWriter fileWriter = new FileWriter(ConfigManager.getProperty("output_file_path") + fileName, false);
            Rio.write(model, fileWriter, RDFFormat.TURTLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void printModel(Model m) {
        Rio.write(m, System.out, RDFFormat.TURTLE);
    }
    
    
}
