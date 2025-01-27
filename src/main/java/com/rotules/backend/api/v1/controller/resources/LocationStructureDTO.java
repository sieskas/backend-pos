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
                        new ColumnSchemaDTO("ID", "Number", 10, true),
                        new ColumnSchemaDTO("Nom", "String", 50, true),
                        new ColumnSchemaDTO("Type", "Dropdown", true, getLocationTypes())
                )
        );

        this.address = new SectionDTO("Adresse",
                List.of(
                        new ColumnSchemaDTO("Adresse", "String", 100, false),
                        new ColumnSchemaDTO("Ville", "Dropdown", false, List.of("Paris", "Marseille", "Lyon"))
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
        private final List<String> dropdownOptions;

        public ColumnSchemaDTO(String name, String type, int maxLength, boolean required) {
            this(name, type, maxLength, required, null);
        }

        public ColumnSchemaDTO(String name, String type, boolean required, List<String> dropdownOptions) {
            this(name, type, 0, required, dropdownOptions);
        }

        public ColumnSchemaDTO(String name, String type, int maxLength, boolean required, List<String> dropdownOptions) {
            this.name = name;
            this.type = type;
            this.maxLength = maxLength;
            this.required = required;
            this.dropdownOptions = dropdownOptions;
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

        public List<String> getDropdownOptions() {
            return dropdownOptions;
        }
    }
}
