/*
 * Copyright (c) 2019.
 * Created by Vladislav Zraevskij
 *
 * This file is part of XReader.
 *
 *     XReader is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     XReader is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with XReader.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 *
 */

package org.apache.poi.ss.formula;

import org.apache.poi.ss.formula.eval.ConcatEval;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.eval.IntersectionEval;
import org.apache.poi.ss.formula.eval.PercentEval;
import org.apache.poi.ss.formula.eval.RangeEval;
import org.apache.poi.ss.formula.eval.RelationalOperationEval;
import org.apache.poi.ss.formula.eval.TwoOperandNumericOperation;
import org.apache.poi.ss.formula.eval.UnaryMinusEval;
import org.apache.poi.ss.formula.eval.UnaryPlusEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.formula.functions.Indirect;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.ConcatPtg;
import org.apache.poi.ss.formula.ptg.DividePtg;
import org.apache.poi.ss.formula.ptg.EqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterEqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterThanPtg;
import org.apache.poi.ss.formula.ptg.IntersectionPtg;
import org.apache.poi.ss.formula.ptg.LessEqualPtg;
import org.apache.poi.ss.formula.ptg.LessThanPtg;
import org.apache.poi.ss.formula.ptg.MultiplyPtg;
import org.apache.poi.ss.formula.ptg.NotEqualPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.PercentPtg;
import org.apache.poi.ss.formula.ptg.PowerPtg;
import org.apache.poi.ss.formula.ptg.RangePtg;
import org.apache.poi.ss.formula.ptg.SubtractPtg;
import org.apache.poi.ss.formula.ptg.UnaryMinusPtg;
import org.apache.poi.ss.formula.ptg.UnaryPlusPtg;

import java.util.HashMap;
import java.util.Map;

final class OperationEvaluatorFactory {
   
   private static final Map _instancesByPtgClass = initialiseInstancesMap();
   
   private static Map initialiseInstancesMap() {
      HashMap m = new HashMap(32);
      put(m, EqualPtg.instance, RelationalOperationEval.EqualEval);
      put(m, GreaterEqualPtg.instance, RelationalOperationEval.GreaterEqualEval);
      put(m, GreaterThanPtg.instance, RelationalOperationEval.GreaterThanEval);
      put(m, LessEqualPtg.instance, RelationalOperationEval.LessEqualEval);
      put(m, LessThanPtg.instance, RelationalOperationEval.LessThanEval);
      put(m, NotEqualPtg.instance, RelationalOperationEval.NotEqualEval);
      put(m, ConcatPtg.instance, ConcatEval.instance);
      put(m, AddPtg.instance, TwoOperandNumericOperation.AddEval);
      put(m, DividePtg.instance, TwoOperandNumericOperation.DivideEval);
      put(m, MultiplyPtg.instance, TwoOperandNumericOperation.MultiplyEval);
      put(m, PercentPtg.instance, PercentEval.instance);
      put(m, PowerPtg.instance, TwoOperandNumericOperation.PowerEval);
      put(m, SubtractPtg.instance, TwoOperandNumericOperation.SubtractEval);
      put(m, UnaryMinusPtg.instance, UnaryMinusEval.instance);
      put(m, UnaryPlusPtg.instance, UnaryPlusEval.instance);
      put(m, RangePtg.instance, RangeEval.instance);
      put(m, IntersectionPtg.instance, IntersectionEval.instance);
      return m;
   }
   
   private static void put(Map m, OperationPtg ptgKey, Function instance) {
      m.put(ptgKey, instance);
   }
   
   public static ValueEval evaluate(OperationPtg ptg, ValueEval[] args, OperationEvaluationContext ec) {
      if (ptg == null) {
         throw new IllegalArgumentException("ptg must not be null");
      } else {
         Function result = (Function) _instancesByPtgClass.get(ptg);
         if (result != null) {
            return result.evaluate(args, ec.getRowIndex(), (short) ec.getColumnIndex());
         } else if (ptg instanceof AbstractFunctionPtg) {
            AbstractFunctionPtg fptg = (AbstractFunctionPtg) ptg;
            short functionIndex = fptg.getFunctionIndex();
            switch (functionIndex) {
               case 148:
                  return Indirect.instance.evaluate(args, ec);
               case 255:
                  return UserDefinedFunction.instance.evaluate(args, ec);
               default:
                  return FunctionEval.getBasicFunction(functionIndex)
                                     .evaluate(args, ec.getRowIndex(), (short) ec.getColumnIndex());
            }
         } else {
            throw new RuntimeException("Unexpected operation ptg class (" + ptg.getClass().getName() + ")");
         }
      }
   }
   
}