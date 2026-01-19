# Kiln Flowmap

🌏 | [中文文档](Kiln-Flowmap-CHN.md)

## Introduction

This is a simple document written by Kurv, which explains kiln's complex mechanic that can be sliced into two independent parts.

***It's strongly recommend not to edit the code of kiln unless you completely figured it out.***

---

## Kiln Container Content Handle Flowmap

*NOTE: If you haven't installed `Mermaid`, you can also see the image version at [here](Kiln-InputCheck-Flowmap.svg).*

***We can't guarantee that images are always up-to-date.***

```mermaid
graph TB;
    subgraph Step1-Check
        %% Declaration
        Input(Check Container Content);
        
        %% Flows
        Input --> |Content Not Changed| Skip;
        Input --> |Changed| Continue;
    end
    
    subgraph Step2-Change Type Check
        %% Declaration
        Choice(Is the attribute, or just<br>the quantity of item changed?);
        GoToStep3["Iterate Container Input Slots"];
        GoToStep5["Onward To UI Aspect"];
        
        %% Flows
        Continue --> Choice;
        
        Choice --> |Quantity Changed| Variant-QC --> GoToStep5;
        Choice --> |Attribute Changed| Variant-AC --> GoToStep3;
    end
    
    subgraph Step3-Input Slots Iteration
        %% Declaration
        Iteration-Choice("Check Content's Recipe Type");
        Valid-Pre-handle["Set <code>inputState</code> as <code>VALID</code>"];
        Invalid-Pre-handle["Set <code>inputState</code> as <code>INVALID</code>"];
        
        %% Flows
        GoToStep3 --> Iteration-Choice;
        
        Iteration-Choice --> |KilnRecipe| Valid-Pre-handle --> Normal-Step1;
        Iteration-Choice --> |Others| Invalid-Pre-handle --> Unexpected-Check;
    end
    
    subgraph Step3a-Valid Content Iteration
        %% Declaration
        Normal-Step1["Get recipe"];
        Normal-Step2["Iterate forward one index"];
        Normal-Step3["Push new recipes to Calculator"];
        
        %% Flows
        Normal-Step1 --> |Iteration Unfinished| Normal-Step2 --> Iteration-Choice;
        Normal-Step1 --> |Iteration Finished| Normal-Step3 --> GoToStep4;
    end
    
    subgraph Step3b-Unexpected Content Process
        %% Declaration
        Unexpected-Check("Get the detailed type of recipe");
        Tip-Handle["Set Recipe as <i><code>tipRecipe</code></i>"];
        Invalid-Handle["Set Recipe as <i><code>noRecipe</code></i>"];
        Unexpected-Iteration-Final-Handle["Terminate iteration early"];
        GoToStep4["Go to step 4"];
        
        %% Flows
        Unexpected-Check --> |Blasting Recipe| Tip-Handle --> Unexpected-Iteration-Final-Handle;
        Unexpected-Check --> |No Recipe| Invalid-Handle --> Unexpected-Iteration-Final-Handle;
        
        Unexpected-Iteration-Final-Handle --> GoToStep4;
    end
    
    subgraph "Step4-Commit Dirty Data & Notify UI"
        %% Declaration
        Final-Data-Process["Execute <code>setChanged</code> Method"];
        To-UI-Aspect["Onward to <code>quickMoveStack</code><br>method(in <code>KilnMenu</code>)"];
    
        %% Flows
        GoToStep4 --> |"Data get dirtied"| Final-Data-Process --> To-UI-Aspect;
    end
    
    GoToStep5 --> To-UI-Aspect;
```

*For UI([KilnMenu](../client/ui/KilnMenu.java))'s behavior, please go to go see the detailed implementation*.

---

## Standard Operating Procedures Flowmaps

**Kiln is implemented in a component way, where the model is responsible for recording progress, while the calculator only cares about logical operations.**

About **Overall Process Demonstration**, please go to <u>[`KilnBlockEntity#serverTick`](../blockstates/KilnBlockEntity.java)</u> to see details.
`serverTick` itself acts as a linear scheduler that invokes the following SOPs in order.

Also, **<u>[state deduction(`#deduceProcessState`)](../blockstates/KilnBlockEntity.java)</u> also won't get covered**. It's quite straightforward and not complex at all.

### Progress Calculation

*TIP:*
*This step is executed by <u>[`KilnProgressCalculator#calculateRates`](../blockstates/components/KilnProgressCalculator.java)</u>.*

#### Initialization

```mermaid
graph TB;
    subgraph Calculation Initialization Check
        %% Declaration
        Init-Start["Start recipes' iteration"];
        Init-Invalidation-Check("Get whether the type of recipe is valid");
        To-Unexpected["Go to Unexcepted Handling"];
        To-Normal["Go to Normal Initialization"];
        
        %% Flows
        Init-Start --> Init-Invalidation-Check;

        Init-Invalidation-Check --> |"emptyRecipe & tipRecipe"| To-Unexpected; 
        Init-Invalidation-Check --> |<code>KilnRecipe</code>| To-Normal;
    end
    
    NOTE["Note: Step 'Calculation Initialization Check' actually checks all recipes through iteration,<br>and actually is the same iteration as the step 'Normal Recipe Process',<br>updating the flowmap will make these steps extremely complex, so it's better not to."]
        
    subgraph Unexpected Recipe Handling
        %% Declaration
        Init-Unexpected-Universal-Pre-handle["Terminates iteration"];
        Init-Unexpected-Universal-Handle["Assign value <code>NORMAL_PROGRESS_RATE</code><br>to <code>lastProcessFactor</code>,<br> and cancel balancing state,<br>preventing data corruption"];
        Init-Unexpected-Universal-Return["Returns result as both two progress decreased<br><code>NORMAL_PROGRESS_RATE</code> percents(behaves like <code>COOLDOWN</code>)"];
        Init-Unexpected-Return-Choice("Get the detailed type of Recipe");
        Init-Unexpected-Return-Null["Return <code>ResultType</code> as <code>INVALID</code>"];
        Init-Unexpected-Return-Blast["Return <code>ResultType</code> as <code>BLAST_TIP</code>"];
        Skip["Skip the rest calculations"]
            
        %% Flows
        To-Unexpected --> Init-Unexpected-Universal-Pre-handle --> Init-Unexpected-Universal-Handle; 
        Init-Unexpected-Universal-Handle --> Init-Unexpected-Universal-Return --> Init-Unexpected-Return-Choice;

        Init-Unexpected-Return-Choice --> |emptyRecipe| Init-Unexpected-Return-Null --> Skip;
        Init-Unexpected-Return-Choice --> |tipRecipe| Init-Unexpected-Return-Blast --> Skip;
    end
        
    subgraph Normal Recipe Process 
        %% Declaration
        Init-Normal-Choice("Is the <code>processFactor</code> of recipe<br>smaller than the standard(<code>STANDARD_PROCESS_FACTOR</code>)?");
        Init-Normal-May-OP-Check["Iterate forward one recipe"];
        Init-Normal-May-OP-Strategy["Use averaging strategy"];
        Init-Normal-Standard["Use multiplying strategy"];
        Init-Finish["Submit the correct <code>processFactor</code>"];
            
        %% Flows
        To-Normal --> Init-Normal-Choice;
        
        Init-Normal-Choice --> |Smaller| Init-Normal-May-OP-Check --> Init-Normal-Choice;
        
        Init-Normal-Choice --> |All recipes are smaller| Init-Normal-May-OP-Strategy --> Init-Finish;
        Init-Normal-Choice --> |Any recipe is bigger| Init-Normal-Standard --> Init-Finish;
    end

    %% Declaration
    To-Calculate-Check["Start final check before calculation"]

    %% Flow
    Init-Finish --> To-Calculate-Check;
    
    subgraph Final Invalidation Check Before Calculation 
        %% Declaration
        Check-Factor("Is <code>processFactor</code> a positive double number?");
        Unexpected-Factor-Value["Return result as progresses no any changes, <code>ResultType</code> as <code>INVALID</code>"];
        Unexpected-Factor-Terminate["Terminate Calculation"];
        To-Calculate["Start calculation"];
        
        %% FLows
        To-Calculate-Check --> Check-Factor;
        
        Check-Factor --> |Non-positive value| Unexpected-Factor-Value --> Unexpected-Factor-Terminate;
        Check-Factor --> |Expected value| To-Calculate;
    end
```

#### Calculation

```mermaid
graph TD;
    subgraph Calculation Pattern Deduction 
        %% Declaration
        Calculation-Pattern-Confirm("Is the current calculate pattern <code>BALANCING</code>?");
        Process-Factor-Compare("Is the current <code>processFactor</code> equals to the last one?");
        Calculation-Default["Calculate progress rates"];
        Calculation-Balance-Pre-handle["Set flag <code>isBalancing</code> to <code>true</code>"];
        Calculation-Balance["Start adjusting visual and real progress"];
        Calculation-Balance-Return["Return the result that contains balanced progresses,<br><code>BALANCING</code> as the value of <code>ResultType</code>,<br>and return the trend of progress depending on the value of rate"]
        Calculation-Default-Pre-handle["Calculate progress rate of this tick"];
        Calculation-Default-Check["The detailed type of <code>processState</code>?"];
        Calculation-Default-Cooldown["Shift the value of rate to negative"];
        Calculation-Default-Return["Add the value of rate to progresses,<br>then return the result,<br>the value of <code>ResultType</code> depends on processState,<br>and return the trend of progress depending on the value of rate"];
        
        %% Flows
        Calculation-Pattern-Confirm --> |Yes| Calculation-Balance;
        Calculation-Pattern-Confirm --> |No| Process-Factor-Compare;
        
        Process-Factor-Compare --> |No| Calculation-Balance-Pre-handle --> Calculation-Balance;
        Process-Factor-Compare --> |Yes| Calculation-Default --> Calculation-Default-Pre-handle --> Calculation-Default-Check;
        
        Calculation-Default-Check --> |<code>WORKING</code>| Calculation-Default-Return;
        Calculation-Default-Check --> |<code>COOLDOWN</code>| Calculation-Default-Cooldown --> Calculation-Default-Return;
        Calculation-Balance --> Calculation-Balance-Return;
    end
```

### Completed Procession Handling

The part which is before the upgrade of progresses also won't get covered, as they are easy to understand.

```mermaid
graph TD;
    subgraph Progress Check 
        %% Declaration
        Check("Is progress bigger than 1D(100%)?");
        
        %% Flows
        Check --> |Yes| Continue;
        Check --> |No| Skip;
    end
    
    subgraph Procession Pre-Check 
        %% Declaration
        Iteration-Start["Start input slots' iteration"];
        
        %% This is logically unnecessary, but still kept for extreme cases.
        Iteration-Check("Is the input that being iterated processable?");
        Iteration-Invalid["Terminate iteration, return <code>false</code> as data upgrade flag"];
        Iteration-Valid-Process["Collect the result item of this input"];
        Iteration-Valid-Choice("Is the iteration ended?");
        Iteration-Continue["Iterate one slot forward"];
        Iteration-Finished["Finish iteration check"];
        To-Emu-Preparation["Go to the preparations before insert emulation"];
        
        %% Flows
        Continue --> Iteration-Start --> Iteration-Check;
        
        Iteration-Check --> |No| Iteration-Invalid;
        Iteration-Check --> |Yes| Iteration-Valid-Process --> Iteration-Valid-Choice;
        
        Iteration-Valid-Choice --> |No| Iteration-Continue --> Iteration-Check;
        Iteration-Valid-Choice --> |Yes| Iteration-Finished;
    end
    
    Iteration-Finished --> To-Emu-Preparation;
    
    subgraph Insert Emulate Preparation
    %% Declaration
        Copy-Output-Slots["Copy the content of output slots<br>to emulate result"];
        Start-Emulation["Start emulation with copy slots"];
        Emu-Iteration-Start["Start an iteration to emulate insertion"];
        
        %% Flows
        To-Emu-Preparation --> Copy-Output-Slots --> Start-Emulation --> Emu-Iteration-Start;
    end
    
    subgraph Insert Emulation 
        %% Declaration
        Emu-Check("Is the result of input can be merged<br>with the output slot that being iterated?");
        Emu-Empty-Check("Is this output slot the<br>first output slot that is empty?");
        Emu-Empty-Yes["Record the index of this output slot"];
        Emu-Invalid-Attempt["Count this attempt of emulation as invalid"];
        Emu-Slot-Forward["Iterate one output slot forward"];
        Emu-Iteration-Check("Does this input have iterated all output slots?");
        Emu-Iteration-Finish-Variant("Are all attempts invalid?");
        Emu-Iteration-Finish-Invalid-Variant("Is there exists a recorded empty output slot?");
        Emu-Iteration-Finish-Invalid-Expected["Merge the result to the recorded slot"];
        Emu-Iteration-Finish-Valid-Merge["Merge the result of input to corresponded output slot"];
        Emu-Failed["Emulation finished with failed result,<br>skip the rest handling, and returning <code>false</code> as return value"];
        Emu-Iteration-End-Check("Is all result of input emulated?");
        Emu-Iteration-Forward["Iterate one result of input forward"];
        Emu-Iteration-End["Fully terminate the emulation, returning<br><code>true</code> to apply the emulated result"];
        
        %% Flows
        Emu-Iteration-Start --> Emu-Check;
        
        Emu-Check --> |False| Emu-Invalid-Attempt --> Emu-Empty-Check;
        Emu-Check --> |True| Emu-Empty-Check;
        
        Emu-Empty-Check --> |Yes| Emu-Empty-Yes --> Emu-Iteration-Check;
        Emu-Empty-Check --> |No| Emu-Iteration-Check;
        
        Emu-Iteration-Check --> |No| Emu-Slot-Forward --> Emu-Check;
        Emu-Iteration-Check --> |Yes| Emu-Iteration-Finish-Variant;
        
        Emu-Iteration-Finish-Variant --> |No| Emu-Iteration-Finish-Valid-Merge --> Emu-Iteration-End-Check;
        Emu-Iteration-Finish-Variant --> |Yes| Emu-Iteration-Finish-Invalid-Variant;
        
        Emu-Iteration-Finish-Invalid-Variant --> |Yes| Emu-Iteration-Finish-Invalid-Expected --> Emu-Iteration-End-Check;
        Emu-Iteration-Finish-Invalid-Variant --> |No| Emu-Failed;
        
        Emu-Iteration-End-Check --> |No| Emu-Iteration-Forward --> Emu-Check;
        Emu-Iteration-End-Check --> |Yes| Emu-Iteration-End;
    end
```
