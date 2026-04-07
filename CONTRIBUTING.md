# Contribution Guidelines

---

## Before Beginning

Welcome to Crisp Sweetberry's basic code style and documentation guidelines!\
This document will help you understand the coding standards we follow in our project.
If you have any questions, feel free to ask.

*These guidelines prioritize long-term maintainability over contribution speed*.\
*If you are looking for a fast-paced or experimental modding environment, this project may not be a good fit*.

---

## About tools & environments

1. **IntelliJ IDEA is strongly recommended** for development.\
   NeoForge’s tooling and official guidance are currently focused on IntelliJ IDEA.

2. If using IntelliJ IDEA, please install the <u>[**Better Highlights**](https://plugins.jetbrains.com/plugin/12895-better-highlights)</u> plugin.\
   We recommend using `*` as a single-line comment prefix to mark critical information or describe the specific purpose of a code region.
   (For region, you can see details from [here](#region)),\
   and using `!` to highlight potential errors, edge cases, or complex exception handling logic.

   ```java
   //  e.g.
   //* This is an important comment!
   //! This is a comment that explains potential errors.
   //? This is a comment that records your confusion, TODO, or the explaination in build script and config.
   ```
   
   Also, you can write *reference* in comments, with pattern `[[]]`, both file and class reference are OK.

3. All nullability annotations must come from **JetBrains annotations**
   (e.g. `@NotNull`, `@Nullable`) to avoid ambiguity caused by mixed annotation systems.

4. After cloning the repo, **please initialize your local repo with these commands**:
   ```shell
    git config filter.hide-i18n.clean "sed 's/^i18n_path=.*/#i18n_path=/'"
    git config filter.hide-i18n.smudge "cat"
   ```
   Then go to [Gradle Property Config](./gradle.properties) to set up your own translation generate location.\
   These commands make sure that your own property config won't commited to git repo, and also Github Repo.

---

## Code Style & Documentation

1. **Zero warnings is our goal.**\
   For unavoidable API issues (e.g. `datafixer` parameters), `null` will be used, but we recommend it being accompanied by:

   ```java
   @SuppressWarnings("ConstantConditions")//! Reason with explanation...
   Foo.bar(null);
   ```

   and an explanatory comment.

2. We don't mind your own code style as long as it could be formatted by any code formatter,
   but we **do hope** you use **CamelCase** for anything that is not a constant or an enum, and **Allman style** as the final submit format.

3. Any modified or newly added functionality **must include Javadocs**, explaining:

   * **Why** it exists

   * **How** it should be used when it couldn't be understood easily from its implementation

   * **What should be avoided** when it has footguns or trade-offs.

   > Concise, clear documentation is preferred over long explanations.

   The following tags are strongly recommended:

   * `@since ${mod version}`
   * `@author ${your name}`

   This is your contribution — take ownership of it.

4. Prioritize **readability over brevity**.\
   Long but meaningful class, method, or variable names are acceptable.

   Constants should:

   * Be placed in the relevant class whenever possible
   * Only be moved to corresponded classes when they are truly universal

5. We do care about the **structure** of your code.\
   Thus, we recommend using the following methods for organizing your code:

      <a id="region"></a>

      ```java
      //region Section of this region

      // Fields, constructors, methods, etc.

      //endregion
      ```

   **This will help everyone to understand the code easier, and faster**.\
   *(In IDEA, you can look the region structure of a unknown code by "Structure" function in the side toolbar)*

6. For codes that are not too complicated, especially the simple inheritance implementation of a class,
   we don't force you to strictly follow the rules above, except the fifth rule, we believe that code should always be clean and readable. 

   *Don't forget to leave `@since + ${Mod Version}` and `@author + ${Your Name}`. This is your contribution!*

7. Temporary messy code are acceptable, as long as it doesn't break boundaries we have declared in out `package-info`s and its complexity is not serious.\
   However, **any code that breaks any of these boundaries will be directly refused.**

8. When in doubt in these aspects, the maintainer’s judgment prevails.

---

## Cooperation Principles

1. If you believe another contributor’s code should be refactored, **discuss it first**.\
   **Unilateral changes can cause unnecessary conflicts and reduce code history clarity**.

2. Modding is not just about modding itself.\
   Discussions don’t have to be limited to pure implementation details.\
   We value human conversations — modders are not robots.

3. Please watch out your attitude when discussing. Being too toxic is neither good for the community nor for yourself.\
   Repeated disrespectful behavior may result in discussion termination or contribution rejection at the maintainer’s discretion.

---

## About submissions

1. **Design philosophy interpretations and final feature decisions are ultimately maintained by the core maintainer(s),
   even when suggestions are reasonable or well-implemented**.

2. **A feature is considered QoL only if it does not introduce new mandatory knowledge, progression steps,
   or configuration dependencies for the average vanilla player**.

> If you have any questions or suggestions about these guidelines, feel free to discuss. Crisp Sweetberry aims to be a project that encourages learning, understanding, and long-term maintainability.
