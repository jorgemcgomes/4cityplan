/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plan.zones;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.plan.ont.OWLEntityNode;
import org.plan.ont.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author Jorge
 */
class PopulationEditor {

    private OWLClass popClass;
    private OWLNamedIndividual popIndividual;
    private OntologyManager om;
    private HashMap<String, JFreeChart> charts;

    PopulationEditor(OWLClass populationClass, OWLNamedIndividual populationIndividual) {
        this.popClass = populationClass;
        this.popIndividual = populationIndividual;
        om = Lookup.getDefault().lookup(OntologyManager.class);
        charts = new LinkedHashMap<String, JFreeChart>();
    }

    Sheet getSheet() {
        Sheet sheet = Sheet.createDefault();

        Sheet.Set dataProps = getDataProperties();
        //dataProps.setValue("tabName", "Simple Indicators");
        sheet.put(dataProps);

        List<Sheet.Set> distributions = getDistributions();
        for (Sheet.Set set : distributions) {
            set.setValue("tabName", "Distributions");
            sheet.put(set);
        }

        return sheet;
    }

    private Sheet.Set getDataProperties() {
        Sheet.Set set = Sheet.createPropertiesSet();
        OWLReasoner reasoner = om.getIndividualsReasoner();
        OWLDataProperty topProp = om.getFactory().getOWLDataProperty(PopSkeleton.Entity.POPULATION_INDICATOR.iri);
        Set<Node<OWLDataProperty>> nodes = reasoner.getSubDataProperties(topProp, false).getNodes();
        for (Node<OWLDataProperty> n : nodes) {
            if (!n.isBottomNode()) {
                OWLDataProperty prop = n.getRepresentativeElement();
                Set<OWLClass> domains = reasoner.getDataPropertyDomains(prop, false).getFlattened();
                if (domains.contains(popClass)) {
                    System.out.println("indicator found: " + prop.toString());
                    DataProp data = getDataProp(prop);
                    set.put(data);
                }
            }
        }
        set.setName("DataProperties");
        set.setDisplayName("Simple Indicators");
	set.setShortDescription("Population simple indicators");
        return set;
    }

    private DataProp getDataProp(OWLDataProperty prop) {
        Set<OWLDataRange> ranges = prop.getRanges(om.getAllOntologies());
        for (OWLDataRange range : ranges) {
            if (range.isDatatype()) {
                OWLDatatype dt = range.asOWLDatatype();
                if (dt.isBoolean()) {
                    return new DataProp<Boolean>(prop, Boolean.class);
                } else if (dt.isDouble()) {
                    return new DataProp<Double>(prop, Double.class);
                } else if (dt.isFloat()) {
                    return new DataProp<Float>(prop, Float.class);
                } else if (dt.isInteger()) {
                    return new DataProp<Integer>(prop, Integer.class);
                } else if (dt.isString()) {
                    return new DataProp<String>(prop, String.class);
                }
            }
        }
        return new DataProp<String>(prop, String.class);
    }

    private class DataProp<T> extends PropertySupport.ReadWrite<T> {

        private OWLDataProperty property;
        private Class<T> type;

        DataProp(OWLDataProperty property, Class<T> type) {
            // TODO nome baseado na label e descricao no comment
            super(property.getIRI().toString(),
                    type, OWLEntityNode.displayName(property), OWLEntityNode.mainDescription(property));
            this.property = property;
            this.type = type;
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            OWLReasoner reasoner = om.getIndividualsReasoner();
            Set<OWLLiteral> value = reasoner.getDataPropertyValues(popIndividual, property);
            if (value.isEmpty()) {
                return null;
            }
            OWLLiteral l = value.iterator().next();
            if (type.equals(Boolean.class)) {
                return type.cast(l.parseBoolean());
            } else if (type.equals(Double.class)) {
                return type.cast(l.parseDouble());
            } else if (type.equals(Float.class)) {
                return type.cast(l.parseFloat());
            } else if (type.equals(Integer.class)) {
                return type.cast(l.parseInteger());
            } else {
                return type.cast(l.getLiteral());
            }
        }

        @Override
        public void setValue(T t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Set<OWLLiteral> values = popIndividual.getDataPropertyValues(property, om.getIndividualsOntology());
            if (values.size() > 0) {
                System.out.println("REMOVING...");
                for (OWLLiteral l : values) {
                    OWLDataPropertyAssertionAxiom ax = om.getFactory().getOWLDataPropertyAssertionAxiom(property, popIndividual, l);
                    om.getOWLManager().removeAxiom(om.getIndividualsOntology(), ax);
                }
            }

            OWLDataFactory factory = om.getFactory();
            OWLAxiom ax = null;
            if (type.equals(Boolean.class)) {
                ax = factory.getOWLDataPropertyAssertionAxiom(property, popIndividual, (Boolean) t);
            } else if (type.equals(Double.class)) {
                ax = factory.getOWLDataPropertyAssertionAxiom(property, popIndividual, (Double) t);
            } else if (type.equals(Float.class)) {
                ax = factory.getOWLDataPropertyAssertionAxiom(property, popIndividual, (Float) t);
            } else if (type.equals(Integer.class)) {
                ax = factory.getOWLDataPropertyAssertionAxiom(property, popIndividual, (Integer) t);
            } else {
                ax = factory.getOWLDataPropertyAssertionAxiom(property, popIndividual, (String) t);
            }
            om.getOWLManager().addAxiom(om.getIndividualsOntology(), ax);
            om.getIndividualsReasoner().flush();
            // TODO check consistency?
        }
    }

    private List<Sheet.Set> getDistributions() {
        List<Sheet.Set> setList = new LinkedList<Sheet.Set>();
        OWLReasoner reasoner = om.getIndividualsReasoner();
        OWLObjectProperty topProp = om.getFactory().getOWLObjectProperty(PopSkeleton.Entity.HAS_DISTRIBUTION.iri);
        Set<Node<OWLObjectPropertyExpression>> nodes = reasoner.getSubObjectProperties(topProp, false).getNodes();
        System.out.println(nodes.size() +  " Distro properties");
        for (Node<OWLObjectPropertyExpression> n : nodes) {
            if (!n.isBottomNode() && !n.getRepresentativeElement().isAnonymous()) {
                OWLObjectProperty prop = n.getRepresentativeElement().asOWLObjectProperty();
                DefaultPieDataset dataset = new DefaultPieDataset();
                String propName = OWLEntityNode.displayName(prop);
                Sheet.Set s = getDistribution(prop, dataset);
                System.err.println(s.getProperties().length);
                JFreeChart pieChart = ChartFactory.createPieChart(propName, dataset, true, false, false);
                charts.put(propName, pieChart);
                setList.add(s);
            }
        }
        return setList;
    }

    private Sheet.Set getDistribution(OWLObjectProperty hasDistro, DefaultPieDataset dataset) {
        Sheet.Set set = Sheet.createPropertiesSet();
	set.setName(hasDistro.getIRI().toString());
	set.setDisplayName(OWLEntityNode.displayName(hasDistro));
	set.setShortDescription(OWLEntityNode.mainDescription(hasDistro));
        OWLReasoner reasoner = om.getIndividualsReasoner();
        OWLOntology individuals = om.getIndividualsOntology();
        OWLObjectProperty indicatorLevel = om.getFactory().getOWLObjectProperty(PopSkeleton.Entity.INDICATOR_LEVEL.iri);
        Set<OWLClassExpression> ranges = hasDistro.getRanges(om.getAllOntologies());
        // Ranges da propriedade
        for (OWLClassExpression disj : ranges) {
            // Expressoes de cada range
            for (OWLClassExpression exp : disj.asDisjunctSet()) {
                if (exp instanceof OWLObjectSomeValuesFrom) {
                    OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) exp;
                    // Range do indicator level
                    if (some.getProperty().equals(indicatorLevel)) {
                        OWLClassExpression r = some.getFiller();
                        Set<OWLNamedIndividual> flattened = reasoner.getInstances(r, false).getFlattened();
                        // Possibilidades de cada range
                        for (OWLNamedIndividual val : flattened) {
                            IRI partitionIRI = IRI.create(PopSkeleton.ONTOLOGY
                                    + "#Partition_" + hasDistro.getIRI().getFragment()
                                    + "_" + popIndividual.getIRI().getFragment()
                                    + "_" + val.getIRI().getFragment());
                            OWLNamedIndividual partition = om.getFactory().getOWLNamedIndividual(partitionIRI);
                            if (!individuals.containsIndividualInSignature(partitionIRI)) {
                                // Colocar a particao na ontologia
                                OWLObjectPropertyAssertionAxiom ax1 =
                                        om.getFactory().getOWLObjectPropertyAssertionAxiom(hasDistro, popIndividual, partition);
                                OWLObjectPropertyAssertionAxiom ax2 =
                                        om.getFactory().getOWLObjectPropertyAssertionAxiom(indicatorLevel, partition, val);
                                om.getOWLManager().addAxiom(individuals, ax1);
                                om.getOWLManager().addAxiom(individuals, ax2);
                            }
                            
                            PartitionProp p = new PartitionProp(partition, val, dataset);
                            set.put(p);
                        }
                    }
                }
            }
        }
        om.getIndividualsReasoner().flush();
        return set;
    }

    private class PartitionProp extends PropertySupport.ReadWrite<Integer> {

        private OWLNamedIndividual partition;
        private OWLDataProperty popNumber;
        private DefaultPieDataset pie;
        private String valueName;

        PartitionProp(OWLNamedIndividual partition, OWLNamedIndividual value, DefaultPieDataset pie) {
            super(partition.getIRI().toString(),
                    Integer.class, OWLEntityNode.displayName(value), OWLEntityNode.mainDescription(value));
            this.partition = partition;
            this.popNumber = om.getFactory().getOWLDataProperty(PopSkeleton.Entity.POPULATION_NUMBER.iri);
            this.pie = pie;
            this.valueName = value.getIRI().getFragment();
        }

        @Override
        public Integer getValue() throws IllegalAccessException, InvocationTargetException {
            Set<OWLLiteral> values = partition.getDataPropertyValues(popNumber, om.getIndividualsOntology());
            Integer val = null;
            if (values.size() == 1) {
                val = values.iterator().next().parseInteger();
            }
            if (val == null || val <= 0) {
                if (pie.getIndex(valueName) != -1) {
                    pie.remove(valueName);
                }
            } else {
                pie.setValue(valueName, val);
            }

            return val;
        }

        @Override
        public void setValue(Integer t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Set<OWLLiteral> values = partition.getDataPropertyValues(popNumber, om.getIndividualsOntology());
            if (values.size() > 0) {
                for (OWLLiteral l : values) {
                    OWLDataPropertyAssertionAxiom ax = om.getFactory().getOWLDataPropertyAssertionAxiom(popNumber, partition, l);
                    om.getOWLManager().removeAxiom(om.getIndividualsOntology(), ax);
                }
            }
            OWLDataPropertyAssertionAxiom ax = om.getFactory().getOWLDataPropertyAssertionAxiom(popNumber, partition, t);
            om.getOWLManager().addAxiom(om.getIndividualsOntology(), ax);
            om.getIndividualsReasoner().flush();
            if (t == null || t <= 0) {
                if (pie.getIndex(valueName) != -1) {
                    pie.remove(valueName);
                }
            } else {
                pie.setValue(valueName, t);
            }
        }
    }

    Collection<JFreeChart> getCharts() {
        return charts.values();
    }
}
