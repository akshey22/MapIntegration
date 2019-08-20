/*
 * Copyright 2014 CITIUS <http://citius.usc.es>, University of Santiago de Compostela.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.function.impl;


import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.Node;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.Transition;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.function.NodeExpander;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.function.NodeFactory;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.model.function.TransitionFunction;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.util.F;
import org.unyde.mapintegrationlib.InternalNavigation.indoornav.hipster.util.Function;

public class LazyNodeExpander<A,S,N extends Node<A,S,N>> implements NodeExpander<A,S,N> {
    private TransitionFunction<A,S> tf;
    private NodeFactory<A,S,N> factory;

    /**
     * Initializes this node expander taking a node factory and the transition function
     * of the problem.
     *
     * @param tf transition function
     * @param factory node factory
     */
    public LazyNodeExpander(TransitionFunction<A, S> tf, NodeFactory<A, S, N> factory) {
        this.tf = tf;
        this.factory = factory;
    }

    @Override
    public Iterable<N> expand(final N node) {
        // The default expansion of a node consists of
        // computing the successor transitions of the current state and
        // generating the associated nodes for each successor
        return F.map(tf.transitionsFrom(node.state()), new Function<Transition<A, S>, N>() {
            @Override
            public N apply(Transition<A, S> t) {
                return factory.makeNode(node, t);
            }
        });
    }

    /**
     * @return transition function
     */
    public TransitionFunction<A, S> getTransitionFunction() {
        return tf;
    }

    /**
     * @return classic node factory
     */
    public NodeFactory<A, S, N> getNodeFactory() {
        return factory;
    }
}
