# Newproject

## Claude Extension

This project includes a Claude AI extension that integrates with the [Anthropic Claude API](https://www.anthropic.com/).

### `ClaudeExtension.java`

A Java class that provides a simple interface for sending messages to Claude and receiving AI-generated responses.

#### Requirements

- Java 8 or higher
- An [Anthropic API key](https://console.anthropic.com/)

#### Usage

**Compile:**
```bash
javac ClaudeExtension.java
```

**Run:**
```bash
export ANTHROPIC_API_KEY=your_api_key_here
java ClaudeExtension "Your message here"
```

**Integrate in your code:**
```java
ClaudeExtension claude = new ClaudeExtension(System.getenv("ANTHROPIC_API_KEY"));
String response = claude.sendMessage("Explain how Java works.");
System.out.println(response);
```
