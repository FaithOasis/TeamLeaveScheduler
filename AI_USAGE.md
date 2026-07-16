# AI Usage

## Tools used

- **ChatGPT (OpenAI)** – used as a coding assistant for:
    - Help with writing unit tests (I had never written tests before).
    - Debugging Maven dependency issues.
    - Suggestions for HTML/CSS structure.

## How I used AI

I designed and built the application – the architecture, the business logic, the data model, the REST APIs. I used AI as a helper for specific tasks where I needed guidance:

1. **Unit testing:** I had never written unit tests before. I asked for help structuring test cases. The AI provided a template, which I adapted and integrated into my project.

2. **Maven issues:** When I encountered a persistent build error with a non‑existent dependency (`spring-boot-thymeleaf`), I asked the AI for help diagnosing it. The AI helped me identify that I was using the wrong artifact name and guided me to remove it since I didn't actually need Thymeleaf.

3. **HTML front‑end:** I used the AI to suggest a basic HTML structure for the dashboard, which I customised to match my design and endpoints.

## Two most useful prompts

1. *"Help me write unit tests for my leave service; I've never written tests before."*  
   – This gave me a starting point for Mockito tests, which I then modified and expanded to match my own business logic.

2. *"My Maven build fails with 'spring-boot-thymeleaf' not found."*  
   – The AI identified the root cause (wrong artifact name) and helped me resolve it quickly.

## One case where AI got it wrong

- The AI initially suggested using a `leaveBalance` field in the `Employee` entity and deducting days from it. I had already decided not to track a leave balance because the task only required enforcing the 30% rule and overlap rule. I ignored that suggestion and kept my model simple. When I later asked for tests, the AI initially included balance‑related checks, but I removed them to match my actual domain.
