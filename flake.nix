{
  inputs = {
    typelevel-nix.url = "github:typelevel/typelevel-nix";
    nixpkgs.follows = "typelevel-nix/nixpkgs";
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlay ];
        };
      in
      {
        devShell = pkgs.devshell.mkShell {
          imports = [ typelevel-nix.typelevelShell ];
          name = "fs2-dom-shell";
          typelevelShell = {
            jdk.package = pkgs.jdk17;
            nodejs.enable = true;
            nodejs.package = pkgs.nodejs;
          };
          packages = [ pkgs.chromium pkgs.chromedriver pkgs.firefox pkgs.geckodriver ];
        };
      }
    );
}
