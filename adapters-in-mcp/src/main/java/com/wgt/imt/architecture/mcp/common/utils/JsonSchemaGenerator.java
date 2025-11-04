package com.wgt.imt.architecture.mcp.common.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilitaire de génération dynamique d'un <strong>JSON Schema</strong> à partir d'une classe Java.
 *
 * <h2>Qu'est-ce que JSON Schema ?</h2>
 * <p>JSON Schema est un standard (RFC) qui décrit la structure d'un document JSON :
 * types des propriétés, champs obligatoires, formats attendus, etc.
 * C'est le format qu'utilise la spécification MCP pour le champ {@code inputSchema}
 * d'un outil, permettant au LLM de comprendre quels arguments fournir.</p>
 *
 * <h2>Principe de fonctionnement</h2>
 * <p>Cette classe utilise la <strong>réflexion Java</strong> ({@link java.lang.reflect}) pour
 * inspecter les champs d'une classe à l'exécution et les convertir en JSON Schema :</p>
 * <ol>
 *   <li>Liste les champs non statiques de la classe</li>
 *   <li>Mappe chaque type Java vers son type JSON Schema équivalent ({@link #TYPE_MAPPINGS})</li>
 *   <li>Ajoute le format si applicable ({@link #FORMAT_MAPPINGS})</li>
 *   <li>Enrichit avec les annotations {@code @Schema} (description, exemple)</li>
 *   <li>Marque comme "required" les champs annotés {@code @NotNull}</li>
 * </ol>
 *
 * <h2>Exemple — classe Java en entrée</h2>
 * <pre>
 * {@code
 * public class GetOneClientArguments {
 *     @Schema(description = "Client identifier", example = "550e8400-...")
 *     @NotNull
 *     private UUID identifier;
 * }
 * }
 * </pre>
 *
 * <h2>JSON Schema produit en sortie</h2>
 * <pre>
 * {
 *   "type": "object",
 *   "properties": {
 *     "identifier": {
 *       "description": "Client identifier",
 *       "example": "550e8400-...",
 *       "type": "string",
 *       "format": "uuid"
 *     }
 *   },
 *   "required": ["identifier"]
 * }
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonSchemaGenerator {

    /**
     * Table de correspondance entre les types Java primitifs/courants et les types JSON Schema.
     *
     * <p>JSON Schema ne connaît que 6 types de base : {@code string}, {@code number},
     * {@code integer}, {@code boolean}, {@code array}, {@code object}.
     * Cette map fait le pont entre le monde Java et ces types.</p>
     */
    private static final Map<Class<?>, String> TYPE_MAPPINGS = Map.ofEntries(
            Map.entry(String.class, "string"),
            Map.entry(UUID.class, "string"),
            Map.entry(Integer.class, "integer"),
            Map.entry(int.class, "integer"),
            Map.entry(Long.class, "integer"),
            Map.entry(long.class, "integer"),
            Map.entry(Short.class, "integer"),
            Map.entry(short.class, "integer"),
            Map.entry(Byte.class, "integer"),
            Map.entry(byte.class, "integer"),
            Map.entry(Double.class, "number"),
            Map.entry(double.class, "number"),
            Map.entry(Float.class, "number"),
            Map.entry(float.class, "number"),
            Map.entry(Boolean.class, "boolean"),
            Map.entry(boolean.class, "boolean")
    );

    /**
     * Table de correspondance entre les types Java et les formats JSON Schema.
     *
     * <p>Le champ {@code format} affine le type JSON Schema (ex : un UUID est un {@code string}
     * de format {@code uuid}). Ces formats sont définis par la spécification JSON Schema et
     * permettent aux outils (et au LLM) de valider plus précisément les valeurs.</p>
     */
    private static final Map<Class<?>, String> FORMAT_MAPPINGS = Map.of(
            UUID.class, "uuid",
            Long.class, "int64",
            long.class, "int64",
            Integer.class, "int32",
            int.class, "int32",
            Float.class, "float",
            float.class, "float",
            Double.class, "double",
            double.class, "double"
    );

    /**
     * Point d'entrée public : génère le JSON Schema complet pour une classe donnée.
     *
     * <p>Retourne une {@link Map} représentant le JSON Schema, prête à être sérialisée en JSON
     * par Jackson. Si {@code clazz} est {@code null}, retourne une map vide.</p>
     *
     * @param clazz La classe Java à introspecter (ex : {@code GetOneClientArguments.class})
     * @return Un {@link Map} conforme au format JSON Schema, ou une map vide si {@code clazz} est null
     */
    public static Map<String, Object> generateSchema(final Class<?> clazz) {
        return Optional.ofNullable(clazz)
                .map(JsonSchemaGenerator::buildSchema)
                .orElse(Collections.emptyMap());
    }

    /**
     * Construit le JSON Schema racine pour une classe.
     *
     * <p>Structure produite :</p>
     * <pre>
     * {
     *   "type": "object",
     *   "properties": { ... },   // présent uniquement si des champs existent
     *   "required": [ ... ]      // présent uniquement si des champs sont @NotNull
     * }
     * </pre>
     */
    private static Map<String, Object> buildSchema(final Class<?> clazz) {
        final Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        final List<Field> validFields = getValidFields(clazz);

        final Map<String, Object> properties = validFields.stream()
                .collect(Collectors.toMap(
                        Field::getName,
                        JsonSchemaGenerator::generateFieldSchema,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        final List<String> required = validFields.stream()
                .filter(field -> field.isAnnotationPresent(NotNull.class))
                .map(Field::getName)
                .collect(Collectors.toList());

        Optional.of(properties)
                .filter(props -> !props.isEmpty())
                .ifPresent(props -> schema.put("properties", props));

        Optional.of(required)
                .filter(req -> !req.isEmpty())
                .ifPresent(req -> schema.put("required", req));

        return schema;
    }

    /**
     * Filtre les champs pertinents d'une classe pour la génération du schéma.
     *
     * <p>Sont exclus : les champs statiques (constantes de classe) et {@code serialVersionUID}
     * (champ technique de {@link java.io.Serializable}, sans sens fonctionnel).</p>
     */
    private static List<Field> getValidFields(final Class<?> clazz) {
        return Stream.of(clazz.getDeclaredFields())
                .filter(JsonSchemaGenerator::isValidField)
                .collect(Collectors.toList());
    }

    private static boolean isValidField(final Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !"serialVersionUID".equals(field.getName());
    }

    /**
     * Génère le sous-schéma JSON pour un champ Java individuel.
     *
     * <p>Pour chaque champ, on produit un objet JSON Schema tel que :</p>
     * <pre>
     * {
     *   "description": "...",   // depuis @Schema(description = "...")
     *   "example": "...",       // depuis @Schema(example = "...")
     *   "type": "string",       // déduit du type Java via TYPE_MAPPINGS
     *   "format": "uuid"        // déduit du type Java via FORMAT_MAPPINGS (optionnel)
     * }
     * </pre>
     *
     * @param field Le champ Java à analyser par réflexion
     * @return Un {@link Map} représentant le schéma du champ
     */
    private static Map<String, Object> generateFieldSchema(final Field field) {
        final Map<String, Object> fieldSchema = new LinkedHashMap<>();

        // Enrichissement depuis @Schema (annotation Swagger/OpenAPI réutilisée ici pour sa sémantique)
        Optional.ofNullable(field.getAnnotation(Schema.class))
                .ifPresent(schema -> {
                    Optional.of(schema.description())
                            .filter(desc -> !desc.isEmpty())
                            .ifPresent(desc -> fieldSchema.put("description", desc));

                    Optional.of(schema.example())
                            .filter(ex -> !ex.isEmpty())
                            .ifPresent(ex -> fieldSchema.put("example", ex));
                });

        // Type JSON Schema déduit du type Java
        fieldSchema.put("type", getJsonType(field.getType()));

        // Format JSON Schema (optionnel, affine le type)
        Optional.ofNullable(getJsonFormat(field.getType()))
                .ifPresent(format -> fieldSchema.put("format", format));

        return fieldSchema;
    }

    /**
     * Convertit un type Java en type JSON Schema.
     *
     * <p>Logique de résolution (dans l'ordre) :</p>
     * <ol>
     *   <li>Cherche dans {@link #TYPE_MAPPINGS} (types primitifs et courants)</li>
     *   <li>Si le type est un tableau ou une {@link Collection}, retourne {@code "array"}</li>
     *   <li>Par défaut, retourne {@code "object"} (pour les POJO imbriqués)</li>
     * </ol>
     *
     * @param javaType Le type Java à convertir
     * @return Le type JSON Schema correspondant
     */
    private static String getJsonType(final Class<?> javaType) {
        return Optional.ofNullable(TYPE_MAPPINGS.get(javaType))
                .or(() -> Optional.of(javaType)
                        .filter(type -> type.isArray() || Collection.class.isAssignableFrom(type))
                        .map(type -> "array"))
                .orElse("object");
    }

    /**
     * Détermine le format JSON Schema pour les types nécessitant une précision supplémentaire.
     *
     * @param javaType Le type Java à analyser
     * @return Le format JSON Schema (ex : {@code "uuid"}, {@code "int32"}) ou {@code null} si non applicable
     */
    private static String getJsonFormat(final Class<?> javaType) {
        return FORMAT_MAPPINGS.get(javaType);
    }
}