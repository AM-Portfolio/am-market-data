---
trigger: always_on
glob: "**/*plan*"
description: "Mandatory requirements for Implementation Plans"
---
# Implementation Plan Standards

## 1. Visual Architecture (MANDATORY)
- **Diagrams Required**: Every implementation plan **MUST** include an architecture diagram. 
    - Use **Mermaid** for logic flows, sequence diagrams, or class structures.
    - Use **`generate_image`** for high-level system architecture or UI mockups.
- **Visual Clarity**: The diagram should clearly illustrate the "Proposed Changes" or the interaction between components.

## 2. Structure
- **Proposed Changes**:
    - Group by Module/Component.
    - Explicitly state **New**, **Modified**, and **Deleted** files.
- **Verification Plan**:
    - **Automated**: Provide exact commands (e.g., `mvn test -pl ...`).
    - **Manual**: Step-by-step verification guide for the user.

## 3. Approval
- Do not proceed to strict Execution without User Approval of the plan.
