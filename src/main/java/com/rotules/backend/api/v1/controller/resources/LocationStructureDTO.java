package com.rotules.backend.api.v1.controller.resources;

import com.rotules.backend.domain.LocationTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocationStructureDTO {
    private final SectionDTO locationInfo;
    private final SectionDTO address;

    public LocationStructureDTO() {
        this.locationInfo = new SectionDTO("Informations générales",
                List.of(
                        new ColumnSchemaDTO("ID", "Number", 10, true, "id", null),
                        new ColumnSchemaDTO("Nom", "String", 50, true, "label", null),
                        new ColumnSchemaDTO("Type", "Dropdown", true, "type", getLocationTypes())
                )
        );

        this.address = new SectionDTO("Adresse",
                List.of(
                        new ColumnSchemaDTO("Adresse", "String", 100, false, "address", null),
                        new ColumnSchemaDTO("Ville", "Dropdown", false, "city", List.of("Paris", "Marseille", "Lyon"))
                )
        );
    }

    public SectionDTO getLocationInfo() {
        return locationInfo;
    }

    public SectionDTO getAddress() {
        return address;
    }

    private static List<String> getLocationTypes() {
        return Arrays.stream(LocationTypeEnum.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public static class SectionDTO {
        private final String label;
        private final List<ColumnSchemaDTO> columnsSchema;

        public SectionDTO(String label, List<ColumnSchemaDTO> columnsSchema) {
            this.label = label;
            this.columnsSchema = columnsSchema;
        }

        public String getLabel() {
            return label;
        }

        public List<ColumnSchemaDTO> getColumnsSchema() {
            return columnsSchema;
        }
    }


    public static class ColumnSchemaDTO {
        private final String name;
        private final String type;
        private final int maxLength;
        private final boolean required;
        private final String apiField;  // Nouveau champ
        private final List<String> dropdownOptions;
        private Object value;  // Pour stocker la valeur

        public ColumnSchemaDTO(String name, String type, int maxLength, boolean required, String apiField) {
            this(name, type, maxLength, required, apiField, null);
        }

        public ColumnSchemaDTO(String name, String type, boolean required, String apiField, List<String> dropdownOptions) {
            this(name, type, 0, required, apiField, dropdownOptions);
        }

        public ColumnSchemaDTO(String name, String type, int maxLength, boolean required, String apiField, List<String> dropdownOptions) {
            this.name = name;
            this.type = type;
            this.maxLength = maxLength;
            this.required = required;
            this.apiField = apiField;
            this.dropdownOptions = dropdownOptions;
            this.value = null;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public boolean isRequired() {
            return required;
        }

        public String getApiField() {
            return apiField;
        }

        public List<String> getDropdownOptions() {
            return dropdownOptions;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
