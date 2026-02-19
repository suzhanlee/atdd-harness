# How AI Impacts Skill Formation

**Judy Hanwen Shen, Alex Tamkin**

*Anthropic*

February 3, 2026

arXiv:2601.20245v2 [cs.CY] 1 Feb 2026

---

## Abstract

AI assistance produces significant productivity gains across professional domains, particularly for novice workers. Yet how this assistance affects the development of skills required to effectively supervise AI remains unclear. Novice workers who rely heavily on AI to complete unfamiliar tasks may compromise their own skill acquisition in the process. We conduct randomized experiments to study how developers gained mastery of a new asynchronous programming library with and without the assistance of AI.

We find that AI use impairs conceptual understanding, code reading, and debugging abilities, without delivering significant efficiency gains on average. Participants who fully delegated coding tasks showed some productivity improvements, but at the cost of learning the library. We identify six distinct AI interaction patterns, three of which involve cognitive engagement and preserve learning outcomes even when participants receive AI assistance. Our findings suggest that AI-enhanced productivity is not a shortcut to competence and AI assistance should be carefully adopted into workflows to preserve skill formation — particularly in safety-critical domains.

---

## 1. Introduction

Since the industrial revolution, skills in the labor market have continually shifted in response to the introduction of new technology; the role of workers often shifts from performing the task to supervising the task [Autor et al., 2001]. For example, the automation of factory robots has enabled humans to move from manual labor to supervision, and accounting software has enabled professionals to move from performing raw calculations to identifying better bookkeeping and tax strategies. In both scenarios, humans are responsible for the quality of the final product and are liable for any errors [Bleher and Braun, 2022]. Even as automation changes the process of completing tasks, technical knowledge to identify and fix errors remains extremely important.

As AI promises to be a catalyst for automation and productivity in a wide range of applications, from software engineering to entrepreneurship [Dell'Acqua et al., 2023, Peng et al., 2023, Cui et al., 2024, Otis et al., 2024, Brynjolfsson et al., 2025], the impacts of AI on the labor force are not yet fully understood. Although more workers rely on AI to improve their productivity, it is unclear whether the use of AI assistance in the workplace might hinder core understanding of concepts or prevent the development of skills necessary to supervise automated tasks. Although most studies have focused on the end product of AI assistance (e.g., lines of code written, quality of ideas proposed), an equally important, if not more crucial question is how process of receiving AI assistance impacts workers. As humans rely on AI for skills such as brainstorming, writing, and general critical thinking, the development of these skills may be significantly altered depending on how AI assistance is used.

Software engineering, in particular, has been identified as a profession in which AI tools can be readily applied and AI assistance significantly improves productivity in daily tasks [Peng et al., 2023, Cui et al., 2024]. Junior or novice workers, in particular, benefit most from AI assistance when writing code. In high-stakes applications, AI written code may be debugged and tested by humans before a piece of software is ready for deployment. This additional verification that enhances safety is only possible when human engineers themselves have the skills to understand code and identify errors. As AI development progresses, the problem of supervising more and more capable AI systems becomes more difficult if humans have weaker abilities to understand code [Bowman et al., 2022]. When complex software tasks require human-AI collaboration, humans still need to understand the basic concepts of code development even if their software skills are complementary to the strengths of AI [Wang et al., 2020]. The combination of persistent competency requirements in high-stakes settings and demonstrated productivity gains from AI assistance makes software engineering an ideal testbed for studying how AI affects skill formation.

We investigate whether using and relying on AI affects the development of software engineering skills [Handa et al., 2025]. Based on the rapid adoption of AI for software engineering, we are motivated by the scenario of engineers acquiring new skills on the job. Although the use of AI tools may improve productivity for these engineers, would they also inhibit skill formation? More specifically, does an AI-assisted task completion workflow prevent engineers from gaining in-depth knowledge about the tools used to complete these tasks?

We run randomized experiments that measure skill formation by asking participants to complete coding tasks with a new library that they have not used before. This represents one way in which engineers acquire and learn new skills, since new libraries are frequently introduced in languages such as Python. We then evaluate their competency with the new library. Our main research questions are:

1. Whether AI improves productivity for a coding task requiring new concepts and skills
2. Whether this use of AI reduces the level of understanding of these new concepts and skills

### 1.1 Our Results

Motivated by the salient setting of AI and software skills, we design a coding task and evaluation around a relatively new asynchronous Python library and conduct randomized experiments to understand the impact of AI assistance on task completion time and skill development.

**Key Finding 1:** Using AI assistance to complete tasks that involve this new library resulted in a **reduction in the evaluation score by 17% or two grade points** (Cohen's d = 0.738, p = 0.010). Meanwhile, we did not find a statistically significant acceleration in completion time with AI assistance.

Through an in-depth qualitative analysis where we watch the screen recordings of every participant in our main study, we explain the lack of AI productivity improvement through the additional time some participants invested in interacting with the AI assistant. Some participants asked up to 15 questions or spent more than 30% of the total available task time on composing queries. We attribute the gains in skill development of the control group to the process of encountering and subsequently resolving errors independently.

**Key Finding 2:** We categorize AI interaction behavior into **six common patterns** and find **three AI interaction patterns that best preserve skill development**. These three patterns of interaction with AI, which resulted in higher scores in our skill evaluation, involve more cognitive effort and independent thinking (for example, asking for explanations or asking conceptual questions only).

---

## 2. Background

### 2.1 The Impacts of AI Usage

Since the widespread availability of ChatGPT, Copilot, Claude, and other advanced conversational assistants in late 2022, AI tools have been widely used in many different domains. Studies examining prompt-based utilization have facilitated a detailed examination of AI's real-world applications [Tamkin et al., 2024, Shen and Guestrin, 2025]. For example, AI tools are being used in professional domains such as software development, education, design, and the sciences [Handa et al., 2025].

#### Productivity Gains

Many studies have found improvements in productivity using these AI assistants:

- Brynjolfsson et al. found that AI-based conversational assistants increased the number of issues call center workers were able to resolve on average by 15%.
- Dell'Acqua et al. found consultants completed 12.2% more tasks on average with the help of AI than without it.
- A consistent pattern emerges: less experienced and lower-skilled workers tend to benefit most [Brynjolfsson et al., 2025, Dell'Acqua et al., 2023, Choi and Schwarcz, 2023, Noy and Zhang, 2023].
- One exception: GPT-4 given to Kenyan small business owners helped high performers improve while worsening results for lower performers [Otis et al., 2024].

For software engineering specifically:
- Peng et al. found crowd-sourced software developers using Copilot completed a task 55.5% faster
- Cui et al. found AI-generated code completions provide a 26.8% boost in productivity as measured by pull requests, commits, and software product builds
- Less experienced coders experienced greater productivity boosts

**The Unknown:** While studies find junior developers experience greater productivity uplift from AI, these workers should be quickly developing new skills in the workplace. Yet the effect of these tools on skill formation of this subgroup remains unknown.

#### Cognitive Offloading

Concerns around AI assistance and skill depletion have been highlighted by recent works:
- Medical professionals trained with AI assistance might not develop keen visual skills to identify certain conditions [Macnamara et al., 2024]
- Surveys show frequent AI use associated with worse critical thinking abilities and increased cognitive offloading [Gerlich, 2025]
- Knowledge workers reported lower cognitive effort and confidence when using generative AI tools [Lee et al., 2025]
- However, these surveys are observational and may not capture causal effects

#### Skill Retention

Adjacent research examines how well humans retain knowledge and skills after AI assistance:
- Wu et al. found performance increases from AI did not persist in subsequent tasks performed independently
- Wiles et al. described AI impact as an "exoskeleton" — enhanced abilities did not persist without AI access

#### Overreliance

When models are fallible yet deployed, human decisions following erroneous model outputs are referred to as "overreliance" [Bušinca et al., 2021, Vasconcelos et al., 2023, Klingbeil et al., 2024]. Methods to reduce overreliance focus on decision-time information such as explanations [Vasconcelos et al., 2023, Reingold et al., 2024] or debate [Kenton et al., 2024].

### 2.2 CS Education and AI Assistance

Measuring skill acquisition is domain dependent. For computer science:
- Most introductory courses measure learning through multiple choice questions, code writing, and code reading/explanations [Cheng et al., 2022]
- Recent work found code interviews and active discussion of students' code yield positive learning outcomes [Kannam et al., 2025]

Observational studies on AI tool usage:
- Poitras et al. found students used AI tools to write code, fix errors, and explain algorithmic concepts
- Students with less coding proficiency were more likely to seek AI assistance
- Some students hesitant due to "dependence worry" [Pan et al., 2024]
- Upper-year students did not rely on LLM assistance and only asked a few questions at the beginning [Prasad et al.]

User studies in professional environments:
- Wang et al. studied different usage patterns between users with and without chat access
- Found rich interaction patterns including interactive debugging, code discussions, and asking specific questions
- Participants ranged from asking ChatGPT to do the entire problem (lowest quality) to asking minimal questions (highest efficiency)

---

## 3. Framework

### Professional Skill Acquisition

The "learning by doing" philosophy has been suggested by many learning frameworks such as Kolb's experiential learning cycle and Problem-Based Learning (PBL) [Kolb, 2014, Schmidt, 1994]. These frameworks connect completion of real-world tasks with learning new concepts and developing new skills.

**Our Model:** We model AI tool assistance as taking a different learning path than without AI. We hypothesize that using AI tools to generate code effectively amounts to taking a shortcut to task completion without a pronounced learning stage.

### AI for Coding Usage Patterns

Prior works found that humans use AI in many different ways for coding: from question answering to writing code to debugging [Poitras et al., 2024, Wang et al., 2020, Pinto et al., 2024]. Different ways of using AI assistance represent different learning paths taken to reach goals.

### Research Questions

Based on this background, we focus on on-the-job learning: settings where workers must acquire new skills to complete tasks. We seek to understand both the impact of AI on productivity and skill formation.

- **RQ1:** Does AI assistance improve task completion productivity when new skills are required?
- **RQ2:** How does using AI assistance affect the development of these new skills?

---

## 4. Methods

### 4.1 Task Selection: Learning Asynchronous Programming with Trio

We designed an experiment around the Python **Trio library** for asynchronous concurrency and I/O processing. This library:
- Is less well known than asyncio (according to StackOverflow questions)
- Involves new concepts (e.g., structured concurrency) beyond just Python fluency
- Is explicitly designed to be easy to use — making it particularly suitable for a learning experiment

We designed five tasks using Trio library for asynchronous programming. After pilot studies, we used the first two tasks in our main study:

1. **Task 1:** Write a timer that prints every passing second while other functions run
   - Introduces core concepts: nurseries, starting tasks, running functions concurrently

2. **Task 2:** Implement a record retrieval function that can handle missing record errors
   - Introduces concepts: error handling and memory channels to store results

We used an online interview platform with an AI assistant chat interface (GPT-4o). The AI assistant has access to participants' current code and can produce full, correct code for both tasks directly when prompted.

### 4.2 Evaluation Design

Based on meta-analysis of evaluations in computer science education [Cheng et al., 2022], we identify four types of questions to assess mastery of coding skills:

| Skill Type | Description | Importance for AI Supervision |
|------------|-------------|-------------------------------|
| **Debugging** | Identify and diagnose errors in code | Crucial for detecting when AI-generated code is incorrect |
| **Code Reading** | Read and comprehend what code does | Enables humans to verify AI-written code before deployment |
| **Code Writing** | Write or pick the right way to write code | Low-level syntax less important; high-level design more important |
| **Conceptual** | Understand core principles behind tools | Critical to assess whether AI code uses appropriate design patterns |

The two tasks cover **7 core concepts** from the Trio library. We designed a quiz with debugging, code reading, and conceptual questions (14 questions, 27 points total). We exclude code writing questions to reduce the impact of syntax errors.

### 4.3 Study Design

We use a between-subjects randomized experiment:

1. **Warm-up Stage:** All participants complete a basic Python coding question (no async concepts, ~4 min avg). No AI access for anyone.

2. **Trio Task Stage:** Maximum 35 minutes to complete two coding tasks.
   - **Treatment Group:** Access to AI assistant
   - **Control Group:** No AI access

3. **Evaluation Stage:** Quiz (no AI for anyone) + demographic survey

**Participants (n=52):**
- 26 per group
- Requirements: >1 year Python experience, code in Python at least weekly, have tried AI coding assistance, never used Trio library

| Attribute | Treatment (%) | Control (%) | Difference |
|-----------|---------------|-------------|------------|
| 1-3 years experience | 2 (7.7%) | 2 (7.7%) | 0 |
| 4-6 years experience | 10 (38.5%) | 9 (34.6%) | 1 (3.8%) |
| 7+ years experience | 14 (53.8%) | 15 (57.7%) | 1 (3.8%) |
| Regular Python use | 18 (69.2%) | 16 (61.5%) | 2 (7.7%) |
| Daily Python use | 8 (30.8%) | 10 (38.5%) | 2 (7.7%) |

---

## 5. Results

### 5.1 Pilot Studies

We conducted 4 pilot studies before the full study:

| Pilot | Platform | n | Tasks | Key Challenges |
|-------|----------|---|-------|----------------|
| A | P1 | 39 | 5 | Non-compliance: 35% used AI in no-AI condition |
| B | P1 | 107 | 5 | Continued non-compliance despite warnings |
| C | P2 | 20 | 5 | Local Item Dependence: participants guessing answers from other questions |
| D | P2 | 20 | 2 | Python syntax delays; only 60% of control finished |

**Pilot D Results:**
- AI group completed tasks faster (Cohen's d=1.11, p=0.03)
- AI group performed significantly worse on knowledge quiz (Cohen's d=1.7, p=0.003)

### 5.2 Main Study

#### Participants
- 58 crowd workers invited, 52 completed
- 53 completed all three parts, 1 disqualified (left 4 blank questions)
- Most participants: bachelor's degree, 25-35 years old, professional or freelance developers

#### Results

**Productivity (Task Time):**
- No significant difference in completion time between groups (p = 0.391)
- AI group average: ~25 min
- Control group average: ~26 min

**Skill Formation (Quiz Score):**
- **Significant reduction in AI group** (Cohen's d=0.738, p=0.01)
- 4.15 point difference on 27-point quiz = **17% score difference or 2 grade points**
- Effect remains significant controlling for warm-up task time (Cohen's d=0.725, p=0.016)

**Task Completion:**
- 4 of 26 control participants did not complete Task 2 within 35 minutes
- All AI condition participants completed both tasks

**Experience Analysis:**
- Control group (No AI) average quiz score is higher across ALL levels of coding experience
- Largest score gap in debugging questions
- Smallest gap in code reading questions

**Self-Reported Experience:**
- Control group reported higher self-reported learning (7-point scale)
- Both groups reported high enjoyment
- AI group found task easier
- Both groups found post-task quiz similarly challenging

---

## 6. Qualitative Analysis

We manually annotated screen recordings of 51 participants in the main study, grouping annotations into task progress events: errors, AI interactions, AI queries, and task completions.

### 6 AI Interaction Patterns

We identified six distinct patterns with starkly different outcomes:

#### Low-Scoring Patterns (Quiz Score < 40%)

| Pattern | n | Time | Score | Description |
|---------|---|------|-------|-------------|
| **AI Delegation** | 4 | 19.5 min | 39% | Wholly relied on AI to write code; completed fastest; few or no errors |
| **Progressive AI Reliance** | 4 | 22 min | 35% | Started with 1-2 questions, eventually delegated all to AI; poor on Task 2 concepts |
| **Iterative AI Debugging** | 4 | 31 min | 24% | Relied on AI to debug/verify; high query count (5-15); slow and poor quiz performance |

#### High-Scoring Patterns (Quiz Score ≥ 65%)

| Pattern | n | Time | Score | Description |
|---------|---|------|-------|-------------|
| **Conceptual Inquiry** | 7 | 22 min | 65% | Only asked conceptual questions; independently resolved errors; fastest high-scoring |
| **Hybrid Code-Explanation** | 3 | 24 min | 68% | Asked for code generation WITH explanations; more time but better understanding |
| **Generation-Then-Comprehension** | 2 | 24 min | 86% | Generated code first, then asked follow-up questions to understand |

### 6.1 AI Interaction

**Interaction Time:**
- Some participants spent significant time (up to 11 minutes) interacting with AI
- Up to 6 minutes composing a single query
- Median completion time only 19 minutes in AI condition

**AI Query Types:**

| Query Type | Count | Example |
|------------|-------|---------|
| Explanation | 79 | "can trio.sleep use partial seconds?" |
| Generation | 51 | "complete get_user_data" |
| Debugging | 9 | "I'm getting a notimplementederror" |
| Capabilities Question | 4 | "Can you see the current question?" |
| Appreciation | 4 | "Great job, we got the expected output" |

- 21/25 participants asked explanation questions (high engagement)
- 16/25 participants used AI to generate code
- 4 participants only asked for code generation with no other questions
- Higher fraction of debugging queries correlates with slower completion and lower quiz scores

### 6.2 Encountering Errors

**Error Frequency:**

| Condition | Task 1 Errors | Task 2 Errors |
|-----------|---------------|---------------|
| AI | 1.0 (0-3) | 0.0 (0-1) |
| No AI | 3.0 (2-5) | 2.0 (0.5-2) |

- AI group median: 1 error total
- Control group median: 3 errors total
- 12 participants completed without errors: only 2 were in control group

**Error Types:**
- Non-Trio errors: NameError, AttributeError (typos, quickly corrected)
- Trio-specific errors: RuntimeWarning (coroutine never awaited), TypeError (coroutine object instead of async function)
- Trio-specific errors force understanding of key concepts

**Key Insight:** Higher frequency of encountering errors in control group leads to more critical thinking about code and library usage. The frequent appearance of Trio-related errors ensures specific concepts are gained during task completion.

### 6.3 Shifts in Active Coding Time

- Participants in AI condition spent much less active time on the task
- Time shifted from coding to reading and understanding AI generations
- Control group achieves higher quiz scores with higher total active time
- Within each condition, higher active time correlates with lower quiz score (experienced programmers spend less time actively coding)

### 6.4 Participant Feedback

**Control Group (No AI):**
- Found task fun
- Instructions good at helping develop understanding of Trio
- "It was fun to learn about asynchronous programming"

**Treatment Group (AI Assistance):**
- Wished they paid more attention to Trio library details
- Reported feeling "lazy"
- "There are still a lot of gaps in my understanding"
- "I wish I'd taken the time to understand the explanations more!"

---

## 7. Discussion

### Main Finding

Using AI to complete tasks that require a new skill (knowledge of a new Python library) **reduces skill formation**.

In a randomized controlled trial:
- Treatment: AI assistant + web search + instructions
- Control: web search + instructions alone

The erosion of conceptual understanding, code reading, and debugging skills suggests workers acquiring new skills should be mindful of their reliance on AI during the learning process.

### Stark Divide in Outcomes

Among AI users, a stark divide exists:
- **High-scoring patterns:** 65%-86% quiz score
- **Low-scoring patterns:** 24%-39% quiz score

High scorers:
- Only asked AI conceptual questions (not code generation)
- Asked for explanations to accompany generated code
- Demonstrated high level of cognitive engagement

### Contrary to Hypothesis

We did not observe a significant performance boost in task completion, despite the AI being able to generate complete solutions when prompted.

**Why?** Heterogeneity in how participants use AI:
- ~20% relied on AI to generate all code → finished faster (19.5 min vs 23 min)
- Others asked many queries (e.g., 15), spent long time composing queries (e.g., 10 min), or asked for explanations → raised average time

### Implications

**Aggressive AI incorporation into workplace can have negative impacts on professional development** if workers do not remain cognitively engaged.

Given time constraints and organizational pressures, junior developers may rely on AI to complete tasks as fast as possible at the cost of real skill development.

**Critical finding:** Biggest difference in test scores is in debugging questions. As companies transition to more AI code writing with human supervision, humans may not possess necessary skills to validate and debug AI-written code if their skill formation was inhibited by using AI in the first place.

---

## 7.1 Future Work

| Limitation | Description |
|------------|-------------|
| **Task Selection** | Single task, chat-based interface. Agentic tools would require even less human participation. Future work should investigate agentic coding tools. |
| **Task Length** | Skill formation takes months to years; we measured over one hour. Future work should study longitudinal real-world skill development. |
| **Participant Realism** | Professional programmers but no real job incentive to learn. Future studies should study novice workers within real companies. |
| **Prompting Skills** | Self-reported AI familiarity, not measured prompting techniques. Extension could test prompting fluency. |
| **Evaluation Design** | Measured through quiz. Other studies could use task completion as alternative evaluation. |
| **Human Assistance** | Did not compare to human assistance counterfactual. Future work can compare AI vs human feedback in various settings. |

### Key Takeaway

For novice workers in software engineering or any industry, our study provides evidence toward the value of **intentional skill development** despite AI tool ubiquity.

Benefits of deploying cognitive effort when encountering learning opportunities:
- Master new tools even if barriers (errors) encountered
- AI can assist while maintaining cognitive effort
- Major LLM services provide learning modes (ChatGPT Study Mode, Claude Code Learning mode)

**Bottom Line:** Participants in the new AI economy must care not only about productivity gains from AI but also the long-term sustainability of expertise development amid proliferation of new AI tools.

---

## 8. Acknowledgments

Thanks to Ethan Perez, Miranda Zhang, Henry Sleight (Anthropic Safety Fellows Program); Matthew Jörke, Juliette Woodrow, Sarah Wu, Elizabeth Childs, Roshni Sahoo, Nate Rush, Julian Michael, Rose Wang for experimental design feedback.

Thanks to Jeffrey Shen, Aram Ebtekar, Minh Le, Mateusz Piotrowski, Nate Rahn, Miles McCain, Jessica Zhu, Alex Wang, John Hewitt, Rosanne Hu, Saffron Huang, Kyle Hsu, Sanjana Srivastava, Jennifer Leung for task testing and feedback.

---

## Appendix A: Participant Details

### A.1 Ethics Review

Protocol reviewed and approved by internal reviewers at Anthropic. Participants:
- Not exposed to risks
- May benefit from learning new software skill
- Gave informed consent during prescreening
- Right to withdraw at any time without penalty
- Still compensated even if fail attention checks or complete incorrectly
- Data completely anonymized

---

## Appendix B: Qualitative Analysis Data and Details

### B.1 Annotation Procedure

51/52 participants uploaded screen recordings. We recorded timestamps for:

| Event | Description | Additional Info |
|-------|-------------|-----------------|
| Task Start | User opens each task | - |
| AI Interaction | User starts typing into AI window | - |
| AI Query | User receives answer from AI | Query description |
| Websearch | User queries search engine | - |
| Paste (Direct) | User pastes output of AI assistant | - |
| Code Copying | User types code using AI output | Code completion |
| Error | Code produces error when run | Error message |
| Interface Error | Development environment or AI assistant error | - |
| Task Completion | Correct output achieved | - |
| Task Submission | User submits task | - |

### B.2 Data Availability

Annotated transcripts available at: https://github.com/safety-research/how-ai-impacts-skill-formation

---

## Appendix C: Evaluation Details

### C.1 Evaluation Design

**Knowledge Categories (7 core Trio concepts):**

1. **Async and await keywords:** When to use await keywords within async functions
2. **Starting Trio functions:** How to spawn tasks, how spawned tasks with different durations behave
3. **Error handling in Trio:** Error propagation patterns, catching errors in child tasks
4. **Coroutines:** Debugging co-routine never awaited errors
5. **Memory channels:** Understanding start_soon doesn't return anything, using dictionaries/lists
6. **Opening and closing a nursery:** Asynchronous context managers
7. **Sequential vs concurrent execution:** Expected behavior of concurrent tasks

---

## Appendix D: Task Details

### Participant Feedback - AI Condition

> "By using the AI assistant, I feel like I got lazy. I didn't read the Trio library intro and code examples as closely as I would have otherwise."

> "I feel stupid from the warmup, but hopefully the other project demonstrated what I can do."

> "I wish I'd taken the time to understand the explanations from Cosmo a bit more!"

> "I'm slow. I think the time limit made me act in a way that wasn't representative to my normal workflow, particularly in the proportion of time spent building mental models vs. obtaining code progressions."

> "I'm surprised that something as simple as understanding the `start_soon` method... I picked up nothing about that in terms of deeper understanding."

### Participant Feedback - Control (No AI) Condition

> "This was a lot of fun but the recording aspect can be cumbersome on some systems."

> "It was fun to learn about asynchronous programming, which I had not encountered before."

> "The programming tasks were very fun and did a good job of helping me understand how Trio works despite never having used it before."

---

## References

- Autor, D., Levy, F., & Murnane, R. (2001). The skill content of recent technological change.
- Becker, J., Rush, N., Barnes, E., & Rein, D. (2025). Measuring the impact of early-2025 AI on experienced open-source developer productivity.
- Bleher, H. & Braun, M. (2022). Diffused responsibility: attributions of responsibility in AI-driven clinical decision support systems.
- Bowman, S. R., et al. (2022). Measuring progress on scalable oversight for large language models.
- Brynjolfsson, E., Li, D., & Raymond, L. (2025). Generative AI at work.
- Bušinca, Z., et al. (2021). Cognitive forcing functions can reduce overreliance on AI.
- Cheng, Q., et al. (2022). Design an assessment for an introductory computer science course.
- Choi, J. H. & Schwarcz, D. (2023). AI assistance in legal analysis.
- Cui, Z. K., et al. (2024). The effects of generative AI on high skilled work.
- Dell'Acqua, F., et al. (2023). Navigating the jagged technological frontier.
- Gerlich, M. (2025). AI tools in society: Impacts on cognitive offloading.
- Handa, K., et al. (2025). Which economic tasks are performed with AI?
- Kannam, S., et al. (2025). Code interviews: A more authentic assessment.
- Kolb, D. A. (2014). Experiential learning.
- Lee, H. P., et al. (2025). The impact of generative AI on critical thinking.
- Macnamara, B. N., et al. (2024). Does AI assistance accelerate skill decay?
- Noy, S. & Zhang, W. (2023). Experimental evidence on the productivity effects of generative AI.
- Otis, N., et al. (2024). The uneven impact of generative AI on entrepreneurial performance.
- Peng, S., et al. (2023). The impact of AI on developer productivity: Evidence from GitHub Copilot.
- Pinto, G., et al. (2024). Developer experiences with a contextualized AI coding assistant.
- Poitras, E., et al. (2024). Generative AI in introductory programming instruction.
- Prasad, S., et al. (2023). Generating programs trivially: student use of LLMs.
- Reingold, O., Shen, J. H., & Talati, A. (2024). Dissenting explanations.
- Schmidt, H. G. (1994). Problem-based learning.
- Shen, J. H. & Guestrin, C. (2025). Societal impacts research requires benchmarks.
- Tamkin, A., et al. (2024). Clio: Privacy-preserving insights into real-world AI use.
- Vasconcelos, H., et al. (2023). Explanations can reduce overreliance on AI systems.
- Wang, D., et al. (2020). From human-human collaboration to human-AI collaboration.
- Wang, W., et al. (2024). Rocks coding, not development.
- Wiles, E., et al. (2024). GenAI as an exoskeleton.
- Wu, S., et al. (2025). Human-generative AI collaboration enhances task performance but undermines intrinsic motivation.

---

*This document was converted from the original PDF: "How AI Impacts Skill Formation" by Judy Hanwen Shen and Alex Tamkin, February 2026.*
