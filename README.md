# QuantumDMN Java SDK

Official Java SDK for the [QuantumDMN](https://quantumdmn.com) DMN Engine API.

## Modules

| Module | Description | Artifact |
|--------|-------------|----------|
| `dmn-java-client` | Plain Java client (Java 17+) | `com.quantumdmn:dmn-java-client` |
| `dmn-java-spring` | Spring Boot Starter (3.x) | `com.quantumdmn:dmn-java-spring` |

## Installation

### Maven

**Plain Java:**
```xml
<dependency>
    <groupId>com.quantumdmn</groupId>
    <artifactId>dmn-java-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Spring Boot:**
```xml
<dependency>
    <groupId>com.quantumdmn</groupId>
    <artifactId>dmn-java-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Plain Java

```java
import com.quantumdmn.client.DmnService;
import com.quantumdmn.client.model.*;

// with static token
DmnService service = new DmnService("https://api.quantumdmn.com", "your-token");

// with token provider (recommended)
DmnService service = new DmnService("https://api.quantumdmn.com", () -> getZitadelToken());

// list projects
List<Project> projects = service.getApi().listProjects();
projects.forEach(p -> System.out.println(p.getName()));

// evaluate decision with type-safe FEEL values
Map<String, FeelValue> context = FeelUtil.contextBuilder()
    .put("age", 25)
    .put("income", 50000.0)
    .put("employed", true)
    .put("name", "John Doe")
    .build();

EvaluateStoredRequest request = new EvaluateStoredRequest().context(context);
EvaluationResult result = service.getApi().evaluateStored(projectId, definitionId, request);

// extract typed values from result
FeelValue output = result.getResult();
if (output.isNumber()) {
    BigDecimal score = output.asNumber();
    System.out.println("Score: " + score);
} else if (output.isString()) {
    String decision = output.asString();
    System.out.println("Decision: " + decision);
}
```

**Type-safe FEEL Values:**

The SDK provides `FeelValue` for type-safe handling of DMN values:
- `FeelValue.ofNumber(25)` - numbers (BigDecimal)
- `FeelValue.ofString("text")` - strings
- `FeelValue.ofBoolean(true)` - booleans
- `FeelValue.ofList(...)` - lists of FeelValues
- `FeelValue.ofContext(...)` - contexts (maps)
- `FeelValue.ofNull()` - null values

Use `FeelUtil.contextBuilder()` for ergonomic context creation.

### Spring Boot

**application.yml:**
```yaml
quantumdmn:
  base-url: https://api.quantumdmn.com
  # Option 1: Zitadel Auth (Auto-configured)
  auth:
    zitadel:
      issuer: https://auth.quantumdmn.com
      key-file: /path/to/service-account.json
      project-id: your-zitadel-project-id  # Required for audience scope

  # Option 2: Static Token
  # token: ${QUANTUMDMN_TOKEN}  
```

The SDK will automatically configure the `DmnService` bean with authentication if `quantumdmn.auth.zitadel.key-file` is present.
You can then simply inject `DmnService` into your components:

```java
@Service
public class DecisionService {
    @Autowired
    private DmnService dmnService;
    
    // ...
}
```

**Custom Token Provider:**
If you need a custom token provider (not Zitadel key file), you can define a bean named `dmnTokenProvider`:
```java
@Configuration
public class DmnConfig {
    @Bean(name = "dmnTokenProvider")
    public Supplier<String> dmnTokenProvider() {
        return () -> "my-custom-token";
    }
}
```

### Usage with DmnEngine (Recommended)

The `DmnEngine` class wraps the underlying API client for a simpler experience.

```java
import com.quantumdmn.client.DmnEngine;
import com.quantumdmn.client.DmnService;

// 1. Setup Authentication 
Supplier<String> tokenProvider = () -> "your-access-token";

// 2. Initialize Engine
DmnService service = new DmnService("https://api.quantumdmn.com", tokenProvider);
DmnEngine engine = new DmnEngine(service, "project-uuid");

// 3. Evaluate Decision
Map<String, Object> context = Map.of("age", 25, "income", 50000);
var results = engine.evaluate("decision-xml-id", null, context);
```

### Authentication with Zitadel JSON Key (Built-in)

The SDK provides a `ZitadelTokenProvider` helper to authenticate using a JSON Key file.

**Prerequisites:**
Ensure `jjwt` dependencies are on your classpath (recommended 0.12.x).

```java
import com.quantumdmn.client.auth.ZitadelTokenProvider;

try {
// Create provider from key file
// Note: projectId is the Zitadel Project ID (different from DMN Project ID) 
// used to request project-specific audience and roles.
ZitadelTokenProvider tokenProvider = new ZitadelTokenProvider(
    "/path/to/service-account.json", // Path to JSON Key
    "https://auth.quantumdmn.com",   // Issuer URL
    "zitadel-project-id"             // Zitadel Project ID (required)
);

// Initialize client
DmnService service = new DmnService(
    "https://api.quantumdmn.com", // Base URL
    tokenProvider
);
    
    // 3. Use Engine
    DmnEngine engine = new DmnEngine(service, "project-uuid");
    // ...
} catch (IOException e) {
    e.printStackTrace();
}
```

## Building

```bash
mvn clean install
```

The client code is generated from `openapi.yaml` during the Maven build using the OpenAPI Generator plugin.

## License

MIT License - see [LICENSE](LICENSE) for details.
