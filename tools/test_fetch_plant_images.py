import unittest

from tools.fetch_plant_images import (
    image_license_allowed,
    image_name_phrases,
    matching_taxon,
    normalized_phrase,
    openverse_record_matches_taxon,
    taxon_phrase,
)


class PlantImageCollectorTests(unittest.TestCase):
    def test_accepts_cc0_without_creator_attribution(self):
        self.assertTrue(image_license_allowed("https://creativecommons.org/publicdomain/zero/1.0/", ""))

    def test_accepts_cc_by_only_when_creator_is_identified(self):
        license_url = "https://creativecommons.org/licenses/by/4.0/"
        self.assertTrue(image_license_allowed(license_url, "A. Photographer"))
        self.assertFalse(image_license_allowed(license_url, ""))

    def test_rejects_noncommercial_and_no_derivatives_licenses(self):
        self.assertFalse(image_license_allowed("https://creativecommons.org/licenses/by-nc/4.0/", "Author"))
        self.assertFalse(image_license_allowed("https://creativecommons.org/licenses/by-nd/4.0/", "Author"))

    def test_species_match_ignores_taxonomic_authorship_but_not_species(self):
        record = {
            "species": "Mentha spicata",
            "acceptedScientificName": "Mentha spicata L.",
            "scientificName": "Mentha spicata L.",
        }
        self.assertTrue(matching_taxon(record, "Mentha spicata"))
        self.assertFalse(matching_taxon(record, "Mentha × piperita"))

    def test_scientific_inventory_names_drop_parenthetical_old_name(self):
        self.assertEqual("Salvia rosmarinus", taxon_phrase("Salvia rosmarinus (نام قدیمی: Rosmarinus officinalis)"))
        self.assertEqual("Citrus sinensis", taxon_phrase("Citrus × sinensis"))

    def test_openverse_requires_species_name_in_title_or_tags(self):
        matching = {
            "title": "Mentha spicata leaves",
            "description": "Fresh green spearmint plant",
            "tags": [{"name": "Mentha spicata"}, {"name": "garden"}],
        }
        wrong_species = {
            "title": "Mentha × piperita",
            "description": "Peppermint leaf",
            "tags": [{"name": "Mentha piperita"}],
        }
        self.assertTrue(openverse_record_matches_taxon(matching, "Mentha spicata"))
        self.assertFalse(openverse_record_matches_taxon(wrong_species, "Mentha spicata"))

    def test_accepts_openverse_cc_by_metadata_with_creator_and_rejects_no_creator(self):
        license_value = "by (https://creativecommons.org/licenses/by/4.0/)"
        self.assertTrue(image_license_allowed(license_value, "Photographer"))
        self.assertFalse(image_license_allowed(license_value, ""))

    def test_image_discovery_has_only_curated_taxon_specific_aliases(self):
        self.assertIn("Acanthopanax senticosus", image_name_phrases("Eleutherococcus senticosus"))
        self.assertIn("Fallopia multiflora", image_name_phrases("Polygonum multiflorum"))
        self.assertIn("sapodilla", image_name_phrases("Manilkara zapota"))
        self.assertEqual(("Mentha spicata",), image_name_phrases("Mentha spicata"))
        self.assertFalse(
            any("Citrus hystrix" in phrase for phrase in image_name_phrases("Citrus micrantha"))
        )

    def test_openverse_alias_support_does_not_match_an_unrelated_species(self):
        from tools.fetch_plant_images import openverse_record_matches_taxon

        synonym = {
            "title": "Acanthopanax senticosus flowers",
            "description": "Siberian plant",
            "tags": [{"name": "Acanthopanax senticosus"}],
        }
        unrelated = {
            "title": "Panax ginseng",
            "description": "Korean ginseng root",
            "tags": [{"name": "Panax ginseng"}],
        }
        self.assertTrue(openverse_record_matches_taxon(synonym, "Eleutherococcus senticosus"))
        self.assertFalse(openverse_record_matches_taxon(unrelated, "Eleutherococcus senticosus"))

    def test_normalization_is_whitespace_insensitive(self):
        self.assertEqual(normalized_phrase("Mentha   spicata"), normalized_phrase("Mentha spicata"))


if __name__ == "__main__":
    unittest.main()
